package com.example.models

import java.time.LocalDateTime

data class User(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val passwordHash: String,
    val createdAt: LocalDateTime
)
