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

data class ItemListState(
    val items: List<Item> = emptyList(),
    val searchText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ItemListViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _state = MutableStateFlow(ItemListState())
    val state: StateFlow<ItemListState> = _state

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect {
                    loadItems(it)
                }
        }
    }
    fun onSearchChange(value: String) {
        _state.value =
            _state.value.copy(
                searchText = value
            )
        searchQuery.value = value
    }

    fun loadItems(search: String = "") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val items = repository.getItems(search)
                _state.value = _state.value.copy(items = items, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }
}