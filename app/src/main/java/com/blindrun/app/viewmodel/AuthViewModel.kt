package com.blindrun.app.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("blindrun_prefs", Context.MODE_PRIVATE)

    var currentUserId: String? = null
    var currentUserRole: String? = null
    var currentUserName: String? = null

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

    fun register(userId: String, password: String, role: String, userName: String): Boolean {
        // 模拟注册，保存用户信息
        saveLogin(userId, role, userName)
        // 额外保存用户名映射（可选）
        prefs.edit().putString("userName_$userId", userName).apply()
        return true
    }

    fun login(userId: String, password: String, role: String): Boolean {
        if (userId.isBlank() || password.isBlank()) return false
        // 从预存中获取用户名（模拟），如果没有则使用默认
        // 在 register 或 login 相关方法中，处理可能的 null
        val userName = prefs.getString("userName_$userId", "用户$userId") ?: "用户$userId"
        saveLogin(userId, role, userName)
        return true
    }
}