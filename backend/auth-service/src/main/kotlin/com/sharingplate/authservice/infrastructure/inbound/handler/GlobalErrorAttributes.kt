package com.sharingplate.authservice.infrastructure.inbound.handler

import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.exception.InvalidCredentialsException
import com.sharingplate.authservice.domain.exception.InvalidTokenException
import com.sharingplate.authservice.domain.exception.UserAlreadyExistsException
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): MutableMap<String, Any> {
        val errorAttributes = super.getErrorAttributes(request, options)
        val error = getError(request)

        when (error) {
            is AdminNotFoundException -> {
                errorAttributes["status"] = HttpStatus.NOT_FOUND.value()
                errorAttributes["error"] = HttpStatus.NOT_FOUND.reasonPhrase
                errorAttributes["message"] = error.message ?: "Admin not found."
            }
            is InvalidCredentialsException -> {
                errorAttributes["status"] = HttpStatus.UNAUTHORIZED.value()
                errorAttributes["error"] = HttpStatus.UNAUTHORIZED.reasonPhrase
                errorAttributes["message"] = error.message ?: "Invalid credentials."
            }
            is InvalidTokenException -> {
                errorAttributes["status"] = HttpStatus.UNAUTHORIZED.value()
                errorAttributes["error"] = HttpStatus.UNAUTHORIZED.reasonPhrase
                errorAttributes["message"] = error.message ?: "Invalid or expired token."
            }
            is UserAlreadyExistsException -> {
                errorAttributes["status"] = HttpStatus.CONFLICT.value()
                errorAttributes["error"] = HttpStatus.CONFLICT.reasonPhrase
                errorAttributes["message"] = error.message ?: "User already exists."
            }
            is IllegalArgumentException -> {
                errorAttributes["status"] = HttpStatus.BAD_REQUEST.value()
                errorAttributes["error"] = HttpStatus.BAD_REQUEST.reasonPhrase
                errorAttributes["message"] = error.message ?: "Invalid request argument."
            }
            else -> {
                errorAttributes["status"] = HttpStatus.INTERNAL_SERVER_ERROR.value()
                errorAttributes["error"] = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
                errorAttributes["message"] = error.message ?: "An unexpected error occurred."
            }
        }
        errorAttributes.remove("exception")
        errorAttributes.remove("trace")
        return errorAttributes
    }
}