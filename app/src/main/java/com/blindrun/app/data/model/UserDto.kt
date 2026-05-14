package com.blindrun.app.data.model

/**
 * 网络/业务层数据模型（DTO）
 * 用于登录/注册请求或界面传输
 */
data class UserDto(
    val username: String,
    val password: String
)