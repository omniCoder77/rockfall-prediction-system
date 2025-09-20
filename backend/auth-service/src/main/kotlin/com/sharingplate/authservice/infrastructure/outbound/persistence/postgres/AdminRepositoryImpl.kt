package com.sharingplate.authservice.infrastructure.outbound.persistence.postgres

import com.sharingplate.authservice.domain.model.Admin
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import com.sharingplate.authservice.infrastructure.outbound.persistence.postgres.entity.AdminEntity
import com.sharingplate.authservice.infrastructure.outbound.persistence.postgres.entity.toEntity
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class AdminRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : AdminRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findAdminById(adminId: UUID): Mono<Admin> {
        logger.debug("Finding admin by ID: {}", adminId)
        return r2dbcEntityTemplate.select(
            Query.query(Criteria.where("admin_id").`is`(adminId)), AdminEntity::class.java
        ).singleOrEmpty().map { it.toAdmin() }
    }

    override fun findAdminByEmail(email: String): Mono<Admin> {
        logger.debug("Finding admin by email: {}", email)
        return r2dbcEntityTemplate.select(
            Query.query(Criteria.where("email").`is`(email)), AdminEntity::class.java
        ).singleOrEmpty().map { it.toAdmin() }
    }

    override fun findAdminByPhoneNumber(phoneNumber: String): Mono<Admin> {
        logger.debug("Finding admin by phone number: {}", phoneNumber)
        return r2dbcEntityTemplate.select(
            Query.query(Criteria.where("phone_number").`is`(phoneNumber)), AdminEntity::class.java
        ).singleOrEmpty().map { it.toAdmin() }
    }

    override fun findAdminByJobId(jobId: String): Mono<Admin> { // New implementation
        logger.debug("Finding admin by job ID: {}", jobId)
        return r2dbcEntityTemplate.select(
            Query.query(Criteria.where("job_id").`is`(jobId)), AdminEntity::class.java
        ).singleOrEmpty().map { it.toAdmin() }
    }

    override fun insert(admin: Admin): Mono<String> {
        val adminEntity = admin.toEntity()
        return r2dbcEntityTemplate.insert(adminEntity)
            .map { it.adminId.toString() }
    }

    override fun updatePassword(adminId: UUID, encodedPassword: String): Mono<Boolean> {
        val update = Update.update("password", encodedPassword)
        val query = Query.query(Criteria.where("admin_id").`is`(adminId))
        return r2dbcEntityTemplate.update(query, update, AdminEntity::class.java).map { it > 0 }
    }
}