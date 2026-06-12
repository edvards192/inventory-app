package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ItemRepository
import com.example.app.domain.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.example.app.domain.util.uriToCompressedFile
import com.example.app.domain.model.Storage
import com.example.app.domain.model.Warehouse

data class ItemDetailState(
    val item: Item? = null,
    val isLoading: Boolean = false,
    val error: String? = null,

    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editEan: String = "",
    val editStorageLocations: List<Storage> = emptyList(),
    val availableWarehouses: List<Warehouse> = emptyList(),
    val selectedWarehouseId: Int? = null
)

class ItemDetailViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state

    fun loadWarehouses() {

        viewModelScope.launch {
            try {
                val warehouses = repository.getWarehouses()
                _state.value = _state.value.copy(availableWarehouses = warehouses)
        } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = "Cannot connect to server.")}
        }
    }

    fun loadItem(id: Int, showLoading: Boolean = true) {

        viewModelScope.launch {

            val wasEditing = _state.value.isEditing
            val currentTitle = _state.value.editTitle
            val currentEan = _state.value.editEan
            val currentStorage = _state.value.editStorageLocations
            val currentWarehouses = _state.value.availableWarehouses
            val currentSelection = _state.value.selectedWarehouseId

            if (showLoading) {
                _state.value = _state.value.copy(
                    isLoading = true
                )
            }

            try {

                val item = repository.getItemById(id)

                _state.value = ItemDetailState(
                    item = item,
                    isEditing = wasEditing,
                    editTitle = if (wasEditing) currentTitle else item.title,
                    editEan = if (wasEditing) currentEan else item.ean,
                    editStorageLocations = if (wasEditing) currentStorage else item.storageLocations,
                    availableWarehouses = currentWarehouses,
                    selectedWarehouseId = currentSelection
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    error = "Cannot connect to server.",
                    isLoading = false
                )
            }
        }
    }
    fun deleteItem(id: Int, onSuccess: () -> Unit) {

        viewModelScope.launch {

            try {
                val success = repository.deleteItem(id)
                if (success) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    error = "Cannot connect to server."
                )
            }
        }
    }
    fun startEditing() {
        val current = _state.value.item ?: return

        _state.value = _state.value.copy(
            isEditing = true,
            editTitle = current.title,
            editEan = current.ean,
            editStorageLocations = current.storageLocations
        )
        loadWarehouses()
    }

    fun cancelEditing() {
        _state.value = _state.value.copy(
            isEditing = false
        )
    }

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(editTitle = value)
    }

    fun onEanChange(value: String) {
        _state.value = _state.value.copy(editEan = value)
    }
    fun updateItem(id: Int, onSuccess: () -> Unit) {

        viewModelScope.launch {

            try {

                val itemSuccess = repository.updateItem(
                    id,
                    Item(
                        id = id,
                        title = _state.value.editTitle,
                        ean = _state.value.editEan
                    )
                )
                if (!itemSuccess) {
                    _state.value = _state.value.copy(error = "Failed to update item.")
                    return@launch
                }
                val storageSuccess =
                    repository.replaceStorage(
                        itemId = id,
                        storage = _state.value.editStorageLocations
                    )
                if (!storageSuccess) {
                    _state.value = _state.value.copy(error = "Failed to update storage.")
                    return@launch
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = "Cannot connect to server.")
            }
        }
    }
    fun deleteImage(itemId: Int, imageId: Int) {
        viewModelScope.launch {
            try {
                val success = repository.deleteImage(imageId)
                if (success) {
                    loadItem(itemId, showLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = "Cannot connect to server.")
            }
        }
    }
    fun uploadImage(
        context: Context,
        itemId: Int,
        uri: Uri
    ) {
        viewModelScope.launch {
            try {
                val file = uriToCompressedFile(
                    context,
                    uri
                )
                val nextSortOrder = _state.value.item
                        ?.images
                        ?.maxOfOrNull { it.sortOrder }
                        ?.plus(1)
                        ?: 0
                val success =
                    repository.uploadImage(
                        itemId = itemId,
                        sortOrder = nextSortOrder,
                        file = file
                    )
                if (success) {
                    loadItem(itemId, showLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = "Cannot connect to server.")
            }
        }
    }
    private fun updateStorageCount(warehouseId: Int, delta: Int) {
        val updated =
            _state.value.editStorageLocations
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
                editStorageLocations = updated
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
    fun selectWarehouse(
        warehouseId: Int
    ) {

        _state.value =
            _state.value.copy(
                selectedWarehouseId = warehouseId
            )
    }
    fun addWarehouse() {
        val selected = _state.value.selectedWarehouseId ?: return
        val warehouse =
            _state.value.availableWarehouses
                .firstOrNull {
                    it.id == selected
                }
                ?: return
        if (
            _state.value.editStorageLocations.any {
                it.warehouseId == selected
            }
        ) {
            return
        }
        _state.value =
            _state.value.copy(

                editStorageLocations =
                    _state.value.editStorageLocations +
                            Storage(
                                warehouseId = warehouse.id,
                                warehouseCode = warehouse.code,
                                warehouseName = warehouse.name,
                                count = 1
                            ),

                selectedWarehouseId = null
            )
    }
    fun setStorageCount(
        warehouseId: Int,
        count: Int
    ) {

        val updated =
            _state.value.editStorageLocations
                .mapNotNull { storage ->

                    if (storage.warehouseId != warehouseId) {
                        storage

                    } else {

                        storage.copy(
                            count = count.coerceAtLeast(0)
                        )
                    }
                }

        _state.value =
            _state.value.copy(
                editStorageLocations = updated
            )
    }
}