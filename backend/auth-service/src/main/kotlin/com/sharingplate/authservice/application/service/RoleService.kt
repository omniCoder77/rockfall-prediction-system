package com.sharingplate.authservice.application.service

import com.sharingplate.authservice.domain.model.Role
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File

@Service
class RoleService(
    private val resourceLoader: ResourceLoader,
    @Value("\${auth.admin-job-id-file:file:/home/rishabh/IdeaProjects/rockfall-prediction-system/backend/auth-service/admin.txt}")
    private val adminJobIdFilePath: String,
    @Value("\${auth.supervisor-job-id-file:file:/home/rishabh/IdeaProjects/rockfall-prediction-system/backend/auth-service/supervisor.txt}")
    private val supervisorJobIdFilePath: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val adminJobIds: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())
    private val supervisorJobIds: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    @PostConstruct
    fun init() {
        loadJobIdsFromFile(adminJobIdFilePath, adminJobIds, "Admin")
        loadJobIdsFromFile(supervisorJobIdFilePath, supervisorJobIds, "Supervisor")
    }

    private fun loadJobIdsFromFile(filePath: String, targetSet: MutableSet<String>, roleName: String) {
        try {
            val resource = resourceLoader.getResource(filePath)
            if (resource.exists()) {
                val file = resource.file
                file.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.isNotBlank()) {
                            targetSet.add(trimmedLine)
                        }
                    }
                }
                logger.info("Successfully loaded {} job IDs from {}: {}", roleName, filePath, targetSet.size)
            } else {
                logger.warn("Job ID file not found for {}: {}", roleName, filePath)
            }
        } catch (e: Exception) {
            logger.error("Error loading {} job IDs from {}: {}", roleName, filePath, e.message, e)
        }
    }

    fun determineRole(jobId: String): Role {
        return when {
            adminJobIds.contains(jobId) -> Role.ADMIN
            supervisorJobIds.contains(jobId) -> Role.SUPERVISOR
            else -> Role.USER // Default role for any other jobId
        }
    }

    // For testing purposes or manual reload if files change
    fun reloadJobIds() {
        logger.info("Reloading job IDs from files.")
        adminJobIds.clear()
        supervisorJobIds.clear()
        init()
    }
}