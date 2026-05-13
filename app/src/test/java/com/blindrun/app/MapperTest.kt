package com.blindrun.app

import com.blindrun.app.data.model.UserDto
import com.blindrun.app.data.model.toEntity
import org.junit.Assert.*
import org.junit.Test

class MapperTest {

    @Test
    fun test_userdto_to_entity() {

        val dto = UserDto(
            id = 1,
            name = "Tom",
            email = "tom@test.com"
        )

        val entity = dto.toEntity()

        assertEquals(dto.id, entity.id)
        assertEquals(dto.name, entity.name)
        assertEquals(dto.email, entity.email)
    }
}