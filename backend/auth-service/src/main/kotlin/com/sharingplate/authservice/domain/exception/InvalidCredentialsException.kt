package com.sharingplate.authservice.domain.exception

class InvalidCredentialsException(message: String = "Invalid email or password") : RuntimeException(message)