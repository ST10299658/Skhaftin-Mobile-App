package com.skhaftin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skhaftin.data.DataRepository
import com.skhaftin.model.FoodListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class FoodListingViewModel : ViewModel() {

    private val repository = DataRepository()

    private val _foodListings = MutableStateFlow<List<FoodListing>>(emptyList())
    val foodListings: StateFlow<List<FoodListing>> = _foodListings

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFoodListings() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getFoodListings { result ->
                _isLoading.value = false
                result.onSuccess {
                    _foodListings.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun createFoodListing(
        donorId: String,
        foodName: String,
        description: String,
        category: String,
        expiryDate: String,
        urgency: String,
        quantity: String,
        pickupLocation: com.skhaftin.model.PickupLocation?,
        pickupTime: String,
        imageUri: Uri?
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.createFoodListing(
                donorId,
                foodName,
                description,
                category,
                expiryDate,
                urgency,
                quantity,
                pickupLocation,
                pickupTime,
                imageUri
            ) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadFoodListings() // Refresh list after success
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun updateFoodListing(id: String, updates: Map<String, Any?>) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.updateFoodListing(id, updates) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadFoodListings()
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun deleteFoodListing(id: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.deleteFoodListing(id) { result ->
                _isLoading.value = false
                result.onSuccess {
                    loadFoodListings()
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
