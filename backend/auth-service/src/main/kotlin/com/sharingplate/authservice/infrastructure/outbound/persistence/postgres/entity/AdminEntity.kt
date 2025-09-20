package com.sharingplate.authservice.infrastructure.outbound.persistence.postgres.entity

import com.sharingplate.authservice.domain.model.Admin
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Table("admin")
data class AdminEntity(
    @Id val adminId: UUID = UUID.randomUUID(),
    val name: String,
    val phoneNumber: String,
    val email: String,
    @Column("password")
    private val _password: String,
    val jobId: String
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getPassword(): String {
        return _password
    }

    override fun getUsername(): String {
        return adminId.toString()
    }

    fun toAdmin() = Admin(
        adminId = this.adminId,
        name = this.name,
        phoneNumber = this.phoneNumber,
        email = this.email,
        password = this.password,
        jobId = this.jobId
    )
}

fun Admin.toEntity(): AdminEntity {
    return AdminEntity(
        adminId = this.adminId,
        name = this.name,
        phoneNumber = this.phoneNumber,
        email = this.email,
        _password = this.password,
        jobId = this.jobId
    )
}