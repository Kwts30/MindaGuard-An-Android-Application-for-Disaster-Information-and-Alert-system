package com.mobiledev.mindaguard.data

import com.mobiledev.mindaguard.backend.MapLayer
import com.mobiledev.mindaguard.backend.MapLayerUiModel

/**
 * Built-in hazard layer catalogue — always shown even without Firebase.
 * Extracted from MapLayerViewModel to keep the ViewModel lean.
 */
fun fallbackMapLayers(): List<MapLayerUiModel> = listOf(
    // ── Flood ────────────────────────────────────────────────────────
    mapLayer("flood_5yr", "Flood – 5 Year", "flood", "layers/flood_5yr.geojson", "#29B6F6", "#0277BD", "5-year flood return period"),
    mapLayer("flood_25yr", "Flood – 25 Year", "flood", "layers/flood_25yr.geojson", "#0288D1", "#01579B", "25-year flood return period"),
    mapLayer("flood_100yr", "Flood – 100 Year", "flood", "layers/flood_100yr.geojson", "#01579B", "#002171", "100-year flood return period"),

    // ── Storm Surge (SSA Advisories) ─────────────────────────────────
    mapLayer("storm_surge_ssa1", "Storm Surge – SSA 1", "storm_surge", "layers/storm_surge_ssa1.geojson", "#FFF176", "#F9A825", "Storm Surge Advisory 1 (0.1–0.5m)"),
    mapLayer("storm_surge_ssa2", "Storm Surge – SSA 2", "storm_surge", "layers/storm_surge_ssa2.geojson", "#FFB74D", "#E65100", "Storm Surge Advisory 2 (0.5–1.0m)"),
    mapLayer("storm_surge_ssa3", "Storm Surge – SSA 3", "storm_surge", "layers/storm_surge_ssa3.geojson", "#EF5350", "#B71C1C", "Storm Surge Advisory 3 (1.0–3.0m)"),
    mapLayer("storm_surge_ssa4", "Storm Surge – SSA 4", "storm_surge", "layers/storm_surge_ssa4.geojson", "#880E4F", "#4A0030", "Storm Surge Advisory 4 (>3.0m)"),

    // ── Earthquake Faults ─────────────────────────────────────────
    mapLayer("earthquake_faults", "Earthquake Active Faults", "earthquake", "layers/earthquake_faults.geojson", "#FF6F00", "#BF360C", "Active fault lines — Davao Region (PHIVOLCS 2025)"),

    // ── Landslide ─────────────────────────────────────────────────────
    mapLayer("landslide", "Landslide Susceptibility", "landslide", "layers/landslide.geojson", "#FF8F00", "#E65100", "Landslide susceptibility zones")
)

private fun mapLayer(
    id: String, name: String, category: String, storagePath: String,
    color: String, strokeColor: String, description: String
) = MapLayerUiModel(
    layer = MapLayer(
        id = id, name = name, category = category,
        storagePath = storagePath, color = color,
        strokeColor = strokeColor, description = description
    )
)

