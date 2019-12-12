package com.fteychene.hexagonal.usersample.domain.security.model

sealed class SecurityError
data class InvalidArgument(val message: String): SecurityError()