package com.yourteam.shareplate.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // "student" or "provider"
    val verified: Boolean = false
)
