package com.sharingplate.authservice.domain.exception

class InvalidTokenException(message: String = "Invalid or expired token") : RuntimeException(message)