package com.blindrun.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blindrun.app.data.db.AppDatabase
import com.blindrun.app.data.db.entity.UserEntity
import com.blindrun.app.data.db.dao.UserDao
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_query_user() = runBlocking {
        val user = UserEntity(1, "Tom", "tom@test.com")

        dao.insertUser(user)
        val result = dao.getAllUsers()

        assertEquals(1, result.size)
        assertEquals("Tom", result[0].name)
    }

    @Test
    fun delete_user() = runBlocking {
        val user = UserEntity(1, "Tom", "tom@test.com")

        dao.insertUser(user)
        dao.deleteUser(user)

        val result = dao.getAllUsers()

        assertTrue(result.isEmpty())
    }
}