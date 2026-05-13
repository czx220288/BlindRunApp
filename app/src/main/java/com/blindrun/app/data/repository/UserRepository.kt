package com.blindrun.app.data.repository

import com.blindrun.app.data.api.ApiService
import com.blindrun.app.data.db.dao.UserDao
import com.blindrun.app.data.db.entity.UserEntity
import com.blindrun.app.data.model.UserDto
import com.blindrun.app.data.model.toEntity

class UserRepository(
    private val api: ApiService,
    private val dao: UserDao
) {

    suspend fun getUsers(): List<UserEntity> {

        // 1️⃣ 先读本地
        val local = dao.getAllUsers()
        if (local.isNotEmpty()) return local

        // 2️⃣ 再请求网络（关键：必须是 List<UserDto>）
        val remote: List<UserDto> = api.getUsers()

        // 3️⃣ 转换（修复 map 泛型错误）
        val entities = remote.map { it.toEntity() }

        // 4️⃣ 写入数据库
        dao.insertUsers(entities)

        return entities
    }

    // 测试用逻辑
    fun formatName(name: String): String {
        return name.trim().uppercase()
    }
}