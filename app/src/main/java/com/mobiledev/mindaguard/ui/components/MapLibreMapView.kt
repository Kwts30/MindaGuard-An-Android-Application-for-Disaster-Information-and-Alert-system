package com.mobiledev.mindaguard.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mobiledev.mindaguard.backend.LayerDownloadState
import com.mobiledev.mindaguard.backend.MapLayerUiModel
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URI

// ── Style JSON constants ──────────────────────────────────────────────────────
const val SATELLITE_STYLE_JSON = """
{
  "version": 8,
  "name": "Satellite",
  "sources": {
    "esri-world-imagery": {
      "type": "raster",
      "tiles": ["https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"],
      "tileSize": 256,
      "attribution": "© Esri",
      "maxzoom": 19
    }
  },
  "layers": [{"id":"satellite","type":"raster","source":"esri-world-imagery"}]
}
"""

const val CLASSIC_STYLE_JSON = """
{
  "version": 8,
  "name": "Classic",
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors",
      "maxzoom": 19
    }
  },
  "layers": [{"id":"osm","type":"raster","source":"osm"}]
}
"""

// ── GeoJSON builder helpers ───────────────────────────────────────────────────
private fun locationsToGeoJson(locations: List<MapLocation>): String {
    val features = JSONArray()
    locations.forEach { loc ->
        features.put(JSONObject().apply {
            put("type", "Feature")
            put("geometry", JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().apply { put(loc.lng); put(loc.lat) })
            })
            put("properties", JSONObject().apply {
                put("id", loc.id)
                put("name", loc.name)
                put("address", loc.address)
            })
        })
    }
    return JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", features)
    }.toString()
}

// Build separate GeoJSON for focused pin vs the rest
private fun alertsToGeoJson(locations: List<MapLocation>, focusedId: String?): Pair<String, String> {
    val normal  = JSONArray()
    val focused = JSONArray()
    locations.forEach { loc ->
        val feature = JSONObject().apply {
            put("type", "Feature")
            put("geometry", JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().apply { put(loc.lng); put(loc.lat) })
            })
            put("properties", JSONObject().apply {
                put("id", loc.id)
                put("name", loc.name)
                put("address", loc.address)
            })
        }
        if (loc.id == focusedId) focused.put(feature) else normal.put(feature)
    }
    fun wrap(arr: JSONArray) = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", arr)
    }.toString()
    return Pair(wrap(normal), wrap(focused))
}

/**
 * Full MapLibre Compose wrapper.
 *
 * @param showEvacPins   whether to show green evacuation-center circles
 * @param showCritPins   whether to show red critical-facility circles
 * @param alertPins      community alert locations to show as orange pins
 * @param focusedAlertId id of the currently-selected alert pin (rendered larger)
 * @param flyToLocation  when non-null, the map animates to that location
 */
@Composable
fun MapLibreMapView(
    modifier: Modifier = Modifier,
    layers: List<MapLayerUiModel>,
    mapStyleJson: String = SATELLITE_STYLE_JSON,
    showEvacPins: Boolean = true,
    showCritPins: Boolean = true,
    alertPins: List<MapLocation> = emptyList(),
    focusedAlertId: String? = null,
    flyToLocation: MapLocation? = null,
    initialLat: Double = 7.0644,
    initialLng: Double = 125.6079,
    initialZoom: Double = 12.0,
    frozen: Boolean = false,
    onMapReady: (MapLibreMap) -> Unit = {}
) {
    val context = LocalContext.current
    MapLibre.getInstance(context)

    val mapView = remember { MapView(context) }
    val mapRef = remember {
        object {
            var map: MapLibreMap? = null
            var currentStyleJson: String = ""
            var lastFlyTo: String? = null
            var lastFocusedAlertId: String? = null
            var lastAlertPinIds: String = ""   // joined IDs — detects list changes
        }
    }

    // ── Hazard GeoJSON overlays ───────────────────────────────────────────────
    fun applyHazardLayers(style: Style) {
        layers.forEach { model ->
            val id = model.layer.id
            val isReady = model.isVisible &&
                model.downloadState is LayerDownloadState.Downloaded &&
                model.localGeoJsonFile?.exists() == true

            runCatching { style.removeLayer("${id}_fill") }
            runCatching { style.removeLayer("${id}_line") }
            runCatching { style.removeSource(id) }

            if (isReady) {
                val file: File = model.localGeoJsonFile ?: return@forEach
                style.addSource(GeoJsonSource(id, URI.create("file://${file.absolutePath}")))
                val fill = runCatching { AndroidColor.parseColor(model.layer.color) }.getOrDefault(AndroidColor.RED)
                val stroke = runCatching { AndroidColor.parseColor(model.layer.strokeColor) }.getOrDefault(AndroidColor.DKGRAY)
                style.addLayer(FillLayer("${id}_fill", id).apply {
                    setProperties(PropertyFactory.fillColor(fill), PropertyFactory.fillOpacity(0.35f))
                })
                style.addLayer(LineLayer("${id}_line", id).apply {
                    setProperties(PropertyFactory.lineColor(stroke), PropertyFactory.lineWidth(1.5f))
                })
            }
        }
    }

    // ── Pin circle layers for Evac & Critical ────────────────────────────────
    fun applyPinLayers(style: Style) {
        // Evacuation pins — green
        runCatching { style.removeLayer("evac_pins") }
        runCatching { style.removeSource("evac_src") }
        if (showEvacPins) {
            style.addSource(GeoJsonSource("evac_src", locationsToGeoJson(EvacCenters)))
            style.addLayer(CircleLayer("evac_pins", "evac_src").apply {
                setProperties(
                    circleRadius(8f),
                    circleColor(AndroidColor.parseColor("#2E7D32")),
                    circleStrokeWidth(2f),
                    circleStrokeColor(AndroidColor.WHITE)
                )
            })
        }

        // Critical pins — red
        runCatching { style.removeLayer("crit_pins") }
        runCatching { style.removeSource("crit_src") }
        if (showCritPins) {
            style.addSource(GeoJsonSource("crit_src", locationsToGeoJson(CriticalFacilities)))
            style.addLayer(CircleLayer("crit_pins", "crit_src").apply {
                setProperties(
                    circleRadius(8f),
                    circleColor(AndroidColor.parseColor("#C62828")),
                    circleStrokeWidth(2f),
                    circleStrokeColor(AndroidColor.WHITE)
                )
            })
        }
    }

    // ── Alert community pins — orange ────────────────────────────────────────
    fun applyAlertPins(style: Style) {
        runCatching { style.removeLayer("alert_pins_focused_ring") }
        runCatching { style.removeLayer("alert_pins_focused") }
        runCatching { style.removeLayer("alert_pins") }
        runCatching { style.removeSource("alert_src") }
        runCatching { style.removeSource("alert_src_focused") }

        if (alertPins.isEmpty()) return

        val (normalJson, focusedJson) = alertsToGeoJson(alertPins, focusedAlertId)

        // Normal alert pins — orange circle
        style.addSource(GeoJsonSource("alert_src", normalJson))
        style.addLayer(CircleLayer("alert_pins", "alert_src").apply {
            setProperties(
                circleRadius(9f),
                circleColor(AndroidColor.parseColor("#F57C00")),   // deep orange
                circleStrokeWidth(2f),
                circleStrokeColor(AndroidColor.WHITE)
            )
        })

        // Focused (selected) alert pin — larger with white ring
        style.addSource(GeoJsonSource("alert_src_focused", focusedJson))
        // Outer white halo
        style.addLayer(CircleLayer("alert_pins_focused_ring", "alert_src_focused").apply {
            setProperties(
                circleRadius(18f),
                circleColor(AndroidColor.parseColor("#33F57C00")),  // semi-transparent orange
                circleStrokeWidth(3f),
                circleStrokeColor(AndroidColor.parseColor("#F57C00"))
            )
        })
        // Inner filled circle
        style.addLayer(CircleLayer("alert_pins_focused", "alert_src_focused").apply {
            setProperties(
                circleRadius(11f),
                circleColor(AndroidColor.parseColor("#F57C00")),
                circleStrokeWidth(2.5f),
                circleStrokeColor(AndroidColor.WHITE)
            )
        })
    }

    fun applyAll(style: Style) {
        applyHazardLayers(style)
        applyPinLayers(style)
        applyAlertPins(style)
    }

    AndroidView(
        factory = { _ ->
            mapView.apply {
                getMapAsync { map ->
                    mapRef.map = map
                    mapRef.currentStyleJson = mapStyleJson
                    map.uiSettings.isCompassEnabled = false
                    map.uiSettings.isLogoEnabled = false
                    map.uiSettings.isAttributionEnabled = false
                    // Freeze: disable all touch interaction
                    if (frozen) {
                        map.uiSettings.isScrollGesturesEnabled = false
                        map.uiSettings.isZoomGesturesEnabled = false
                        map.uiSettings.isRotateGesturesEnabled = false
                        map.uiSettings.isTiltGesturesEnabled = false
                        map.uiSettings.isDoubleTapGesturesEnabled = false
                        map.uiSettings.isQuickZoomGesturesEnabled = false
                    }
                    map.setStyle(Style.Builder().fromJson(mapStyleJson)) { style ->
                        applyAll(style)
                    }
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(initialLat, initialLng))
                        .zoom(initialZoom)
                        .build()
                    onMapReady(map)
                }
            }
        },
        update = { _ ->
            val map = mapRef.map ?: return@AndroidView

            // Fly to selected location
            if (flyToLocation != null && flyToLocation.id != mapRef.lastFlyTo) {
                mapRef.lastFlyTo = flyToLocation.id
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(flyToLocation.lat, flyToLocation.lng), 16.0
                    ), 1000
                )
            }

            val currentAlertPinIds = alertPins.joinToString(",") { it.id }
            val alertsChanged = currentAlertPinIds != mapRef.lastAlertPinIds
            val focusChanged  = focusedAlertId != mapRef.lastFocusedAlertId

            if (alertsChanged) mapRef.lastAlertPinIds = currentAlertPinIds
            if (focusChanged)  mapRef.lastFocusedAlertId = focusedAlertId

            // Switch base style or re-apply overlays
            if (mapRef.currentStyleJson != mapStyleJson) {
                mapRef.currentStyleJson = mapStyleJson
                map.setStyle(Style.Builder().fromJson(mapStyleJson)) { style ->
                    applyAll(style)
                }
            } else if (alertsChanged || focusChanged) {
                // Only alert pins changed — just re-apply alert layer, not full style reload
                map.style?.let { style ->
                    applyHazardLayers(style)
                    applyPinLayers(style)
                    applyAlertPins(style)
                }
            } else {
                map.style?.let { applyAll(it) }
            }
        },
        modifier = modifier
    )

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose { mapView.onStop(); mapView.onDestroy() }
    }
}
