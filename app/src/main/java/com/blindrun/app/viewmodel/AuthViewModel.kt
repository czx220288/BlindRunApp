package com.blindrun.app.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.LoginRequest
import com.blindrun.app.model.User as UserModel
import com.blindrun.app.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("blindrun_prefs", Context.MODE_PRIVATE)

    var currentUserId: String? = null
    var currentUserRole: String? = null
    var currentUserName: String? = null

    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> = _loginResult

    private val _registerResult = MutableStateFlow<Boolean?>(null)
    val registerResult: StateFlow<Boolean?> = _registerResult

    init {
        currentUserId = prefs.getString("userId", null)
        currentUserRole = prefs.getString("role", null)
        currentUserName = prefs.getString("userName", null)
    }

    fun isLoggedIn(): Boolean = currentUserId != null && currentUserRole != null

    fun saveLogin(userId: String, role: String, userName: String) {
        currentUserId = userId
        currentUserRole = role
        currentUserName = userName
        prefs.edit().apply {
            putString("userId", userId)
            putString("role", role)
            putString("userName", userName)
            apply()
        }
    }

    fun logout() {
        currentUserId = null
        currentUserRole = null
        currentUserName = null
        prefs.edit().clear().apply()
    }

    fun register(userId: String, password: String, role: String, userName: String) {
        viewModelScope.launch {
            try {
                val user = UserModel(
                    userId = userId,
                    name = userName,
                    role = role,
                    password = password
                )
                val response = apiService.register(user)
                if (response.isSuccessful && response.body() != null) {
                    saveLogin(userId, role, userName)
                    prefs.edit().putString("userName_$userId", userName).apply()
                    _registerResult.value = true
                } else {
                    _registerResult.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _registerResult.value = false
            }
        }
    }

    fun login(userId: String, password: String, role: String) {
        if (userId.isBlank() || password.isBlank()) {
            _loginResult.value = false
            return
        }
        
        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    userId = userId,
                    password = password,
                    role = role
                )
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    saveLogin(userId, role, user.name)
                    prefs.edit().putString("userName_$userId", user.name).apply()
                    _loginResult.value = true
                } else {
                    _loginResult.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginResult.value = false
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}