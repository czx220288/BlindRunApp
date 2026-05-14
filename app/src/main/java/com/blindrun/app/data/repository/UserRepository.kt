package com.blindrun.app.data.repository

import com.blindrun.app.data.db.dao.UserDao
import com.blindrun.app.data.db.entity.UserEntity

class UserRepository(
    private val userDao: UserDao
) {

    // 注册
    suspend fun register(username: String, password: String): Boolean {
        val exist = userDao.getUserByUsername(username)
        if (exist != null) return false

        userDao.register(
            UserEntity(username = username, password = password)
        )
        return true
    }

    // 登录
    suspend fun login(username: String, password: String): Boolean {
        val user = userDao.login(username, password)
        return user != null
    }
}