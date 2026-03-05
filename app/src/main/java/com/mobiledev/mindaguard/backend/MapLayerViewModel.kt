package com.mobiledev.mindaguard.backend

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import com.mobiledev.mindaguard.data.fallbackMapLayers

// ── Data model for a single hazard layer ─────────────────────────────────────

/**
 * Represents one hazard overlay layer stored in Firebase.
 *
 * Firestore document structure (collection: "map_layers"):
 *   id          : String   – document id, e.g. "storm_surge_5yr"
 *   name        : String   – display name, e.g. "Storm Surge 5-Year"
 *   category    : String   – "storm_surge" | "fault_line" | "landslide"
 *   storagePath : String   – Firebase Storage path, e.g. "layers/storm_surge_5yr.geojson"
 *   color       : String   – hex fill colour, e.g. "#FF5722"
 *   strokeColor : String   – hex stroke colour
 *   description : String   – short description shown in UI
 */
data class MapLayer(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val storagePath: String = "",
    val color: String = "#FF5722",
    val strokeColor: String = "#B71C1C",
    val description: String = ""
)

sealed class LayerDownloadState {
    object Idle : LayerDownloadState()
    data class Downloading(val progress: Float) : LayerDownloadState()
    data class Downloaded(val localFile: File) : LayerDownloadState()
    data class Error(val message: String) : LayerDownloadState()
}

data class MapLayerUiModel(
    val layer: MapLayer,
    val isVisible: Boolean = false,
    val downloadState: LayerDownloadState = LayerDownloadState.Idle,
    val localGeoJsonFile: File? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MapLayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    // All layers fetched from Firestore
    private val _layers = MutableStateFlow<List<MapLayerUiModel>>(emptyList())
    val layers: StateFlow<List<MapLayerUiModel>> = _layers.asStateFlow()

    private val _isLoadingLayers = MutableStateFlow(false)
    val isLoadingLayers: StateFlow<Boolean> = _isLoadingLayers.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Local cache directory for downloaded GeoJSON files */
    private val cacheDir: File get() =
        File(getApplication<Application>().filesDir, "map_layers").also { it.mkdirs() }

    init {
        // Offline-first: show built-in catalogue immediately,
        // restoring any layers that were previously downloaded
        _layers.value = fallbackMapLayers().map { model ->
            val cached = cachedFileFor(model.layer)
            if (cached.exists()) {
                model.copy(
                    downloadState = LayerDownloadState.Downloaded(cached),
                    localGeoJsonFile = cached,
                    isVisible = false   // visible = false until user toggles, but data is ready
                )
            } else model
        }
        // Then try to enrich from Firestore
        fetchLayers()
    }

    // ── Fetch layer catalogue from Firestore ────────────────────────────────

    fun fetchLayers() {
        viewModelScope.launch {
            _isLoadingLayers.value = true
            _errorMessage.value = null
            try {
                val snapshot = db.collection("map_layers").get().await()
                if (snapshot.isEmpty) {
                    // Firestore collection empty — keep fallback
                    _isLoadingLayers.value = false
                    return@launch
                }
                val fetched = snapshot.documents.mapNotNull { doc ->
                    val layer = doc.toObject(MapLayer::class.java)?.copy(id = doc.id)
                        ?: return@mapNotNull null
                    val cachedFile = cachedFileFor(layer)
                    val downloadState = if (cachedFile.exists())
                        LayerDownloadState.Downloaded(cachedFile)
                    else
                        LayerDownloadState.Idle
                    MapLayerUiModel(
                        layer = layer,
                        downloadState = downloadState,
                        localGeoJsonFile = if (cachedFile.exists()) cachedFile else null
                    )
                }
                // Merge: preserve already-cached download state
                val merged = fetched.map { remote ->
                    val existing = _layers.value.find { it.layer.id == remote.layer.id }
                    if (existing != null && existing.downloadState is LayerDownloadState.Downloaded)
                        remote.copy(
                            downloadState = existing.downloadState,
                            localGeoJsonFile = existing.localGeoJsonFile,
                            isVisible = existing.isVisible
                        )
                    else remote
                }
                _layers.value = merged
            } catch (e: Exception) {
                Log.e("MapLayerVM", "fetchLayers — using built-in catalogue", e)
                _errorMessage.value = "Could not reach Firebase. Showing built-in layers."
            } finally {
                _isLoadingLayers.value = false
            }
        }
    }

    // ── Download a layer from Firebase Storage ───────────────────────────────

    fun downloadLayer(layerId: String) {
        val model = _layers.value.find { it.layer.id == layerId } ?: return
        if (model.downloadState is LayerDownloadState.Downloading) return

        viewModelScope.launch {
            updateLayer(layerId) { it.copy(downloadState = LayerDownloadState.Downloading(0f)) }
            val destFile = cachedFileFor(model.layer)
            try {
                val ref = storage.reference.child(model.layer.storagePath)
                ref.getFile(destFile).addOnProgressListener { snap ->
                    val pct = snap.bytesTransferred.toFloat() / snap.totalByteCount.coerceAtLeast(1)
                    updateLayer(layerId) { it.copy(downloadState = LayerDownloadState.Downloading(pct)) }
                }.await()
                updateLayer(layerId) {
                    it.copy(
                        downloadState = LayerDownloadState.Downloaded(destFile),
                        localGeoJsonFile = destFile,
                        isVisible = true           // auto-show after download
                    )
                }
            } catch (e: Exception) {
                Log.e("MapLayerVM", "download failed for $layerId", e)
                destFile.delete()
                updateLayer(layerId) {
                    it.copy(downloadState = LayerDownloadState.Error(e.message ?: "Download failed"))
                }
            }
        }
    }

    // ── Delete a cached layer (free space) ───────────────────────────────────

    fun deleteLocalLayer(layerId: String) {
        val model = _layers.value.find { it.layer.id == layerId } ?: return
        cachedFileFor(model.layer).delete()
        updateLayer(layerId) {
            it.copy(
                downloadState = LayerDownloadState.Idle,
                localGeoJsonFile = null,
                isVisible = false
            )
        }
    }

    // ── Toggle visibility of a downloaded layer ───────────────────────────────

    fun toggleVisibility(layerId: String) {
        updateLayer(layerId) { it.copy(isVisible = !it.isVisible) }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private fun cachedFileFor(layer: MapLayer): File =
        File(cacheDir, "${layer.id}.geojson")

    private fun updateLayer(layerId: String, transform: (MapLayerUiModel) -> MapLayerUiModel) {
        _layers.update { list -> list.map { if (it.layer.id == layerId) transform(it) else it } }
    }
}

