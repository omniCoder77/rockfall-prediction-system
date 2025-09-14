package com.sharingplate.authservice.domain.exception

class UserAlreadyExistsException(email: String = "", phoneNumber: String = "") :
    RuntimeException("User with email '$email' or phone number '$phoneNumber' already exists")