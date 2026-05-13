package com.blindrun.app

import com.blindrun.app.data.db.dao.UserDao
import com.blindrun.app.data.db.entity.UserEntity
import com.blindrun.app.data.model.UserDto
import com.blindrun.app.data.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

// ===== Fake API =====
class FakeApiService : com.blindrun.app.data.api.ApiService {

    override suspend fun getUsers(): List<UserDto> {
        return listOf(
            UserDto(1, "Tom", "t@t.com")
        )
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return user
    }
}

// ===== Fake DAO =====
class FakeUserDao : UserDao {

    private val list = mutableListOf<UserEntity>()

    override suspend fun getAllUsers(): List<UserEntity> = list

    override suspend fun insertUsers(users: List<UserEntity>) {
        list.addAll(users)
    }

    override suspend fun insertUser(user: UserEntity) {
        list.add(user)
    }

    override suspend fun deleteUser(user: UserEntity) {
        list.remove(user)
    }
}

class UserRepositoryTest {

    private val fakeDao = FakeUserDao()
    private val fakeApi = FakeApiService()

    private val repo = UserRepository(fakeApi, fakeDao)

    @Test
    fun test_format_name() {
        val result = repo.formatName("  tom  ")
        assertEquals("TOM", result)
    }

    @Test
    fun test_get_users_from_api() = runBlocking {
        val result = repo.getUsers()

        assertEquals(1, result.size)
        assertEquals("Tom", result[0].name)
    }
}