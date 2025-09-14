package com.sharingplate.authservice.domain.exception

import java.util.*

class AdminNotFoundException(email: String) : RuntimeException("Admin not found with email: $email") {
    constructor(adminId: UUID) : this("Admin not found with ID: $adminId")
}