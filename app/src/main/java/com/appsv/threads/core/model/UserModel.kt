package com.appsv.threads.core.model

data class UserModel(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = "",
    val uid: String?=null
)
