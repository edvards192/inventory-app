package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.remote.toUserMessage
import com.example.app.data.repository.ItemRepository
import com.example.app.data.repository.ReorderRepository
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
    val actionError: String? = null,

    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editEan: String = "",
    val editDescription: String = "",
    val editPrice: String = "",
    val editReorderPoint: String = "0",
    val editStorageLocations: List<Storage> = emptyList(),
    val availableWarehouses: List<Warehouse> = emptyList(),
    val selectedWarehouseId: Int? = null
)

class ItemDetailViewModel : ViewModel() {

    private val repository = ItemRepository()
    private val reorderRepository = ReorderRepository()
    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state

    fun loadWarehouses() {

        viewModelScope.launch {
            try {
                val warehouses = repository.getWarehouses()
                _state.value = _state.value.copy(availableWarehouses = warehouses)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(actionError = e.toUserMessage("Failed to load warehouses."))}
        }
    }

    fun clearActionError() {
        _state.value = _state.value.copy(actionError = null)
    }

    fun loadItem(id: Int, showLoading: Boolean = true) {

        viewModelScope.launch {

            val wasEditing = _state.value.isEditing
            val currentTitle = _state.value.editTitle
            val currentEan = _state.value.editEan
            val currentDescription = _state.value.editDescription
            val currentPrice = _state.value.editPrice
            val currentReorderPoint = _state.value.editReorderPoint
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
                    editDescription = if (wasEditing) currentDescription else item.description.orEmpty(),
                    editPrice = if (wasEditing) currentPrice else item.price?.toString().orEmpty(),
                    editReorderPoint = if (wasEditing) currentReorderPoint else item.reorderPoint.toString(),
                    editStorageLocations = if (wasEditing) currentStorage else item.storageLocations,
                    availableWarehouses = currentWarehouses,
                    selectedWarehouseId = currentSelection
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    error = e.toUserMessage("Failed to load item."),
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
                } else {
                    _state.value = _state.value.copy(actionError = "Failed to delete item.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    actionError = e.toUserMessage("Failed to delete item.")
                )
            }
        }
    }
    fun createReorder(
        itemId: Int,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = reorderRepository.createReorder(itemId, quantity)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to create reorder.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.toUserMessage("Failed to create reorder."))
            }
        }
    }
    fun startEditing() {
        val current = _state.value.item ?: return

        _state.value = _state.value.copy(
            isEditing = true,
            editTitle = current.title,
            editEan = current.ean,
            editDescription = current.description.orEmpty(),
            editPrice = current.price?.toString().orEmpty(),
            editReorderPoint = current.reorderPoint.toString(),
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
        if (value.length <= 13 && value.all(Char::isDigit)) {
            _state.value = _state.value.copy(editEan = value)
        }
    }
    fun onDescriptionChange(value: String) {
        _state.value = _state.value.copy(editDescription = value)
    }
    fun onPriceChange(value: String) {
        _state.value = _state.value.copy(editPrice = value)
    }
    fun onReorderPointChange(value: String) {
        _state.value = _state.value.copy(editReorderPoint = value)
    }
    fun updateItem(id: Int, onSuccess: () -> Unit) {

        viewModelScope.launch {

            try {

                val itemSuccess = repository.updateItem(
                    id,
                    Item(
                        id = id,
                        title = _state.value.editTitle,
                        ean = _state.value.editEan,
                        description = _state.value.editDescription
                            .trim()
                            .takeIf { it.isNotEmpty() },
                        price = _state.value.editPrice.toDoubleOrNull(),
                        reorderPoint = _state.value.editReorderPoint.toIntOrNull() ?: 0
                    )
                )
                if (!itemSuccess) {
                    _state.value = _state.value.copy(actionError = "Failed to update item.")
                    return@launch
                }
                val storageSuccess =
                    repository.replaceStorage(
                        itemId = id,
                        storage = _state.value.editStorageLocations
                    )
                if (!storageSuccess) {
                    _state.value = _state.value.copy(actionError = "Failed to update storage.")
                    return@launch
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(actionError = e.toUserMessage("Failed to update item."))
            }
        }
    }
    fun deleteImage(itemId: Int, imageId: Int) {
        viewModelScope.launch {
            try {
                val success = repository.deleteImage(imageId)
                if (success) {
                    loadItem(itemId, showLoading = false)
                } else {
                    _state.value = _state.value.copy(actionError = "Failed to delete image.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(actionError = e.toUserMessage("Failed to delete image."))
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
                } else {
                    _state.value = _state.value.copy(actionError = "Failed to upload image.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(actionError = e.toUserMessage("Failed to upload image."))
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
                .map { storage ->
                    if (storage.warehouseId != warehouseId) {
                        storage
                    } else {
                        storage.copy(count = count.coerceAtLeast(0))
                    }
                }
        _state.value =
            _state.value.copy(
                editStorageLocations = updated
            )
    }
}
