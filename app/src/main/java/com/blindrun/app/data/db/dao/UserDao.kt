package com.blindrun.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blindrun.app.data.db.entity.UserEntity

@Dao
interface UserDao {

    // 注册（插入用户）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun register(user: UserEntity)

    // 登录查询
    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): UserEntity?

    // 用户是否存在
    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
}