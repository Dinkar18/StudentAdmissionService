package com.dp.padhobihar.utils

/**
 * Generic sealed class for representing UI states with loading, success, and error.
 *
 * Usage pattern:
 *   private val _state = MutableLiveData<UiState<List<Item>>>()
 *   val state: LiveData<UiState<List<Item>>> = _state
 *
 *   fun loadItems() {
 *       _state.value = UiState.Loading
 *       viewModelScope.launch {
 *           try {
 *               val items = repository.getItems()
 *               _state.value = UiState.Success(items)
 *           } catch (e: Exception) {
 *               _state.value = UiState.Error(e.message ?: "Unknown error")
 *           }
 *       }
 *   }
 *
 *   // In Fragment:
 *   viewModel.state.observe(viewLifecycleOwner) { state ->
 *       when (state) {
 *           is UiState.Loading -> showLoading()
 *           is UiState.Success -> showData(state.data)
 *           is UiState.Error -> showError(state.message)
 *       }
 *   }
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
