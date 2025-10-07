package com.skhaftin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.skhaftin.data.DataRepository
import com.skhaftin.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataRepository()
    private val prefs = application.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var userListener: ValueEventListener? = null

    init {
        // Load user from prefs if exists
        val userJson = prefs.getString("user", null)
        if (userJson != null) {
            val user = gson.fromJson(userJson, User::class.java)
            _user.value = user
            // Add real-time listener for user updates
            userListener = repository.addUserListener(user.id) { updatedUser ->
                if (updatedUser != null) {
                    _user.value = updatedUser
                    // Update prefs
                    val updatedJson = gson.toJson(updatedUser)
                    prefs.edit().putString("user", updatedJson).apply()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.loginUser(email, password) { result ->
                _isLoading.value = false
                result.onSuccess {
                    _user.value = it
                    it.token?.let { token -> repository.setAuthToken(token) }
                    // Save to prefs
                    val userJson = gson.toJson(it)
                    prefs.edit().putString("user", userJson).apply()
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, phoneNumber: String?, location: String?, preferredLanguage: String?) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.registerUser(fullName, email, password, phoneNumber, location, preferredLanguage, null) { result ->
                _isLoading.value = false
                result.onSuccess {
                    _user.value = it
                    it.token?.let { token -> repository.setAuthToken(token) }
                    // Save to prefs
                    val userJson = gson.toJson(it)
                    prefs.edit().putString("user", userJson).apply()
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

   public fun logout() {
        // Push user to database
        _user.value?.let { user ->
            repository.saveUserToDatabase(user) { saveResult ->
                saveResult.onFailure { e ->
                    // Log error or handle, but proceed with logout
                    println("Error saving user on logout: ${e.message}")
                }
                // Remove real-time listener
                userListener?.let { listener ->
                    repository.removeUserListener(user.id, listener)
                    userListener = null
                }
            }
        }
        // Clear local
        _user.value = null
        _error.value = null
        prefs.edit().remove("user").apply()
        // Clear local items
        viewModelScope.launch {
            repository.clearLocalItems()
        }
        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut()
    }

    fun clearError() {
        _error.value = null
    }

    fun signInWithGoogle(idToken: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.signInWithGoogle(idToken) { result ->
                _isLoading.value = false
                result.onSuccess {
                    _user.value = it
                    it.token?.let { token -> repository.setAuthToken(token) }
                    // Save to prefs
                    val userJson = gson.toJson(it)
                    prefs.edit().putString("user", userJson).apply()
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }
}
