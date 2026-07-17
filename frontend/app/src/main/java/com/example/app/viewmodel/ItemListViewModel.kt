package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ItemRepository
import com.example.app.domain.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.app.domain.model.Warehouse
import com.example.app.domain.model.StockFilter
import kotlinx.coroutines.FlowPreview

data class ItemListState(
    val items: List<Item> = emptyList(),
    val searchText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sort: String = "updatedDesc",
    val stockFilter: StockFilter = StockFilter.ALL,
    val warehouseIds: List<Int> = emptyList(),
    val availableWarehouses: List<Warehouse> = emptyList(),
    val minQuantity: Int? = null,
    val maxQuantity: Int? = null,
    val page: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
)

@OptIn(FlowPreview::class)
class ItemListViewModel : ViewModel() {

    private val repository = ItemRepository()
    private val _state = MutableStateFlow(ItemListState())
    val state: StateFlow<ItemListState> = _state
    private val searchQuery = MutableStateFlow("")

    init {
        loadWarehouses()
        loadItems()
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect {
                    loadItems(
                        _state.value.searchText,
                        _state.value.sort,
                        _state.value.stockFilter,
                        _state.value.warehouseIds,
                        _state.value.minQuantity,
                        _state.value.maxQuantity
                    )
                }
        }
    }
    fun refresh() {
        _state.value =
            _state.value.copy(
                page = 1,
                hasMore = true,
                items = emptyList(),
                isLoadingMore = false
            )
        loadItems(_state.value.searchText, _state.value.sort, _state.value.stockFilter, _state.value.warehouseIds, _state.value.minQuantity, _state.value.maxQuantity)
    }
    fun onSearchChange(value: String) {
        _state.value =
            _state.value.copy(
                searchText = value,
                page = 1,
                hasMore = true,
                items = emptyList()
            )
        searchQuery.value = value
    }
    fun loadItems(search: String = "",
                  sort: String = _state.value.sort,
                  stockFilter: StockFilter = _state.value.stockFilter,
                  warehouseIds: List<Int> = _state.value.warehouseIds,
                  minQuantity: Int? = _state.value.minQuantity,
                  maxQuantity: Int? = _state.value.maxQuantity,
    ) {
        viewModelScope.launch {
            try {
            _state.value = _state.value.copy(isLoading = true, error = null)
                val currentPage = _state.value.page
                val items = repository.getItems(search, sort, stockFilter, warehouseIds, minQuantity, maxQuantity,
                    page = currentPage, limit = 25)
                val newItems =
                    if (currentPage == 1) {
                        items
                    } else {
                        _state.value.items + items
                    }
                _state.value = _state.value.copy(
                        items = newItems,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = items.size == 25
                    )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value =
                    _state.value.copy(
                        error = "Cannot connect to server\nPlease check your connection.",
                        isLoading = false,
                        isLoadingMore = false
                    )
            }
        }
    }
    fun applyFilters(
        sort: String,
        stockFilter: StockFilter,
        warehouseIds: List<Int>,
        minQuantity: Int?,
        maxQuantity: Int?
    ) {

        _state.value =
            _state.value.copy(
                sort = sort,
                stockFilter = stockFilter,
                warehouseIds = warehouseIds,
                minQuantity = minQuantity,
                maxQuantity = maxQuantity,
                page = 1,
                hasMore = true,
                items = emptyList(),
                isLoadingMore = false
            )

        loadItems(
            search = _state.value.searchText,
            sort = sort,
            stockFilter = stockFilter,
            warehouseIds = warehouseIds,
            minQuantity = minQuantity,
            maxQuantity = maxQuantity
        )
    }
    fun loadWarehouses() {

        viewModelScope.launch {

            try {

                val warehouses =
                    repository.getWarehouses()

                _state.value =
                    _state.value.copy(
                        availableWarehouses = warehouses
                    )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value =
                    _state.value.copy(
                        error = "Cannot connect to server."
                    )
            }
        }
    }
    fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMore || _state.value.isLoading){
            return
        }
        _state.value =
            _state.value.copy(
                isLoadingMore = true,
                page = _state.value.page + 1
            )
        loadItems(
            search = _state.value.searchText,
            sort = _state.value.sort,
            stockFilter = _state.value.stockFilter,
            warehouseIds = _state.value.warehouseIds,
            minQuantity = _state.value.minQuantity,
            maxQuantity = _state.value.maxQuantity
        )
    }
}