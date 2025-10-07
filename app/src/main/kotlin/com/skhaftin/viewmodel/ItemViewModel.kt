package com.skhaftin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skhaftin.data.DataRepository
import com.skhaftin.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.skhaftin.R

class ItemViewModel : ViewModel() {

    private val repository = DataRepository()

    fun setContext(context: android.content.Context) {
        repository.setContext(context)
    }

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getItems { result ->
                _isLoading.value = false
                result.onSuccess {
                    _items.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun createItem(
        name: String,
        quantity: String,
        description: String,
        location: String,
        imageUri: Uri?
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.createItem(name, quantity, description, location, imageUri) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadItems() // Refresh the list after success
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }


    fun updateItem(id: String, name: String?, quantity: String?, description: String?, location: String?) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.updateItem(id, name, quantity, description, location) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadItems() // Reload items after update
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun deleteItem(id: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.deleteItem(id) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadItems() // Reload items after deletion
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadAvailableItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getAvailableItems { result ->
                _isLoading.value = false
                result.onSuccess { items ->
                    if (items.isEmpty()) {
                        // Add sample items for testing
                        val samples = listOf(
                            Item(id = "1", name = "Fresh Apples", quantity = "10", description = "Red apples", location = "Market", imageUrl = null, localImagePath = null, ownerId = "test", uniqueCode = "APP-001"),
                            Item(id = "2", name = "Bread Loaves", quantity = "5", description = "Whole wheat", location = "Bakery", imageUrl = null, localImagePath = null, ownerId = "test", uniqueCode = "BRD-002"),
                            Item(id = "3", name = "Pizza", quantity = "2", description = "Cheese pizza", location = "Restaurant", imageUrl = null, localImagePath = null, ownerId = "test", uniqueCode = "PZA-003"),
                            Item(id = "4", name = "Milk", quantity = "1L", description = "Fresh milk", location = "Store", imageUrl = null, localImagePath = null, ownerId = "test", uniqueCode = "MLK-004"),
                            Item(id = "5", name = "Rice", quantity = "5kg", description = "Basmati rice", location = "Grocery", imageUrl = null, localImagePath = null, ownerId = "test", uniqueCode = "RIC-005")
                        )
                        repository.saveItemsLocally(samples)
                        _items.value = samples
                    } else {
                        _items.value = items
                    }
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun clearItems() {
        _items.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    // Function to upload sample images to Firebase Storage and log URLs
    fun uploadSampleImages() {
        val sampleImages = mapOf(
            "Fresh Apples" to R.drawable.fresh_apples,
            "Bread Loaves" to R.drawable.bread_loaves,
            "Pizza" to R.drawable.pizza,
            "Milk" to R.drawable.milk,
            "Rice" to R.drawable.rice
        )
        sampleImages.forEach { (name, resId) ->
            repository.uploadImageFromResource(resId) { url ->
                if (url != null) {
                    android.util.Log.d("ItemViewModel", "Uploaded $name: $url")
                } else {
                    android.util.Log.e("ItemViewModel", "Failed to upload $name")
                }
            }
        }
    }
}
