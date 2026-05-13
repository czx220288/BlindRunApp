package com.blindrun.app.data.db.dao

import androidx.room.*
import com.blindrun.app.data.db.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false   // ⭐关键
)
abstract class AppDatabase : RoomDatabase()