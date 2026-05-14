package com.blindrun.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blindrun.app.data.db.AppDatabase
import com.blindrun.app.data.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: UserRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repo = UserRepository(db.userDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun register_and_login_success() = runBlocking {

        val registerResult = repo.register("test", "123456")
        assertTrue(registerResult)

        val loginResult = repo.login("test", "123456")
        assertTrue(loginResult)
    }
}