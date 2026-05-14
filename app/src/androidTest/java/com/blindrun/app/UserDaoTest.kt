package com.blindrun.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blindrun.app.data.db.AppDatabase
import com.blindrun.app.data.db.dao.UserDao
import com.blindrun.app.data.db.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRegisterAndLogin() = runBlocking {
        val user = UserEntity(username = "test", password = "123456")

        userDao.register(user)

        val result = userDao.login("test", "123456")

        assertTrue(result != null)
    }

    @Test
    fun testGetUser() = runBlocking {
        val user = UserEntity(username = "abc", password = "111")

        userDao.register(user)

        val result = userDao.getUserByUsername("abc")

        assertEquals("abc", result?.username)
    }
}