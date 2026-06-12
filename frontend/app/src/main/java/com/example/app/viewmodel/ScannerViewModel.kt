package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ScannerState(
    val isLoading: Boolean = false,
    val navigateToItemId: Int? = null,
    val navigateToCreateEan: String? = null,
    val error: String? = null
)

class ScannerViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _state =
        MutableStateFlow(
            ScannerState()
        )

    val state: StateFlow<ScannerState> = _state

    fun onBarcodeScanned(
        ean: String
    ) {

        viewModelScope.launch {

            _state.value =
                _state.value.copy(
                    isLoading = true,
                    error = null
                )

            try {

                val item =
                    repository.getItemByEan(ean)

                if (item != null) {

                    _state.value =
                        ScannerState(
                            navigateToItemId =
                                item.id
                        )

                } else {

                    _state.value =
                        ScannerState(
                            navigateToCreateEan =
                                ean
                        )
                }

            } catch (e: Exception) {

                _state.value =
                    ScannerState(
                        error = "Cannot connect to server."
                    )
            }
        }
    }

    fun clearNavigation() {

        _state.value =
            ScannerState()
    }
}