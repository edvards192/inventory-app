package com.example.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ItemRepository
import com.example.app.domain.model.Item
import com.example.app.domain.util.uriToCompressedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.app.domain.model.Storage
import com.example.app.domain.model.Warehouse
import androidx.core.net.toUri

data class CreateItemState(
    val title: String = "",
    val ean: String = "",
    val imageUris: List<String> = emptyList(),
    val storageLocations: List<Storage> = emptyList(),
    val availableWarehouses: List<Warehouse> = emptyList(),
    val selectedWarehouseId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateItemViewModel : ViewModel() {

    private val repository = ItemRepository()
    private val _state = MutableStateFlow(CreateItemState())
    val state: StateFlow<CreateItemState> = _state
    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value)
    }
    fun onEanChange(value: String) {
        _state.value = _state.value.copy(ean = value)
    }
    fun createItem(context: Context, ean: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val result = repository.createItem(
                    Item(id = 0, title = _state.value.title, ean = ean)
                    )
                if (!result.success || result.itemId == null) {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error = "Failed to create item."
                        )
                    return@launch
                }
                _state.value.imageUris
                    .forEachIndexed { index, uriString ->
                        val file =
                            uriToCompressedFile(
                                context,
                                uriString.toUri()
                            )
                        val success =
                            repository.uploadImage(
                                itemId = result.itemId,
                                sortOrder = index,
                                file = file
                            )
                        if (!success) {
                            _state.value =
                                _state.value.copy(
                                    isLoading = false,
                                    error = "Failed to upload image."
                                )
                            return@launch
                        }
                    }
                val storageSuccess =
                    repository.replaceStorage(
                        itemId = result.itemId,
                        storage = _state.value.storageLocations
                    )
                if (!storageSuccess) {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error = "Failed to save storage."
                        )
                    return@launch
                }
                _state.value = CreateItemState()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, error = "Cannot connect to server.")
            }
        }
    }
    fun loadWarehouses() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(availableWarehouses = repository.getWarehouses())
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = "Cannot connect to server.")
            }
        }
    }
    init {
        loadWarehouses()
    }
    fun selectWarehouse(warehouseId: Int) {
        _state.value =
            _state.value.copy(
                selectedWarehouseId =
                    warehouseId
            )
    }
    fun addWarehouse() {
        val selected = _state.value.selectedWarehouseId ?: return
        val warehouse = _state.value.availableWarehouses
                .firstOrNull { it.id == selected } ?: return
        if (_state.value.storageLocations.any { it.warehouseId == selected } ) {
            return
        }
        _state.value = _state.value.copy(
                storageLocations =
                    _state.value.storageLocations +
                            Storage(
                                warehouseId = warehouse.id,
                                warehouseCode = warehouse.code,
                                warehouseName = warehouse.name,
                                count = 1
                            ),
                selectedWarehouseId = null
            )
    }
    private fun updateStorageCount(warehouseId: Int, delta: Int) {
        val updated =
            _state.value.storageLocations
                .mapNotNull { storage ->
                    if (storage.warehouseId != warehouseId) {
                        storage
                    } else {
                        val newCount =
                            storage.count + delta
                        if (newCount <= 0) {
                            null
                        } else {
                            storage.copy(
                                count = newCount
                            )
                        }
                    }
                }
        _state.value =
            _state.value.copy(
                storageLocations = updated
            )
    }
    fun increaseStorage(warehouseId: Int) {
        updateStorageCount(
            warehouseId,
            1
        )
    }
    fun decreaseStorage(warehouseId: Int) {
        updateStorageCount(
            warehouseId,
            -1
        )
    }
    fun setStorageCount(warehouseId: Int, count: Int) {
        val updated =
            _state.value.storageLocations.map { storage ->
                if (storage.warehouseId == warehouseId) {
                    storage.copy(count = count)
                } else {
                    storage
                }
            }

        _state.value = _state.value.copy(
            storageLocations = updated
        )
    }
    fun addImage(uri: String) {

        _state.value = _state.value.copy(
            imageUris = _state.value.imageUris + uri
        )
    }
    fun removeImage(uri: String) {
        _state.value = _state.value.copy(
            imageUris = _state.value.imageUris.filterNot { it == uri }
        )
    }
}
