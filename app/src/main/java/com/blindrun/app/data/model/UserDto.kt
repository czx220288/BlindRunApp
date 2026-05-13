package com.blindrun.app.data.model

import com.blindrun.app.data.db.entity.UserEntity

data class UserDto(
    val id: Int,
    val name: String,
    val email: String
)

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email
    )
}