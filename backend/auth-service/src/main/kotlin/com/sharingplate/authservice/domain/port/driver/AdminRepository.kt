package com.sharingplate.authservice.domain.port.driver

import com.sharingplate.authservice.domain.model.Admin
import reactor.core.publisher.Mono
import java.util.*

interface AdminRepository {
    fun findAdminById(adminId: UUID): Mono<Admin>
    fun findAdminByEmail(email: String): Mono<Admin>
    fun findAdminByPhoneNumber(phoneNumber: String): Mono<Admin> // New method
    fun insert(admin: Admin): Mono<String>
    fun updatePassword(adminId: UUID, encodedPassword: String): Mono<Boolean>
}