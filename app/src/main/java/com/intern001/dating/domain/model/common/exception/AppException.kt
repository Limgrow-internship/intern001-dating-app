package com.intern001.dating.domain.model.common.exception

open class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

class ValidationException(message: String) : AppException(message)

class InvalidCredentialsException(message: String = "Invalid credentials") : AppException(message)

class AuthenticationException(message: String = "Authentication failed") : AppException(message)

class AuthorizationException(message: String = "Not authorized") : AppException(message)

class RateLimitException(message: String = "Rate limit exceeded") : AppException(message)

class NotFoundException(message: String = "Resource not found") : AppException(message)

class NetworkException(message: String = "Network error") : AppException(message)

class ServerException(message: String = "Server error") : AppException(message)

class TimeoutException(message: String = "Request timeout") : AppException(message)

class ConflictException(message: String = "Resource already exists") : AppException(message)
