package com.sharingplate.authservice.domain.model

import java.util.*

data class Admin(
    val adminId: UUID = UUID.randomUUID(),
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String
)
