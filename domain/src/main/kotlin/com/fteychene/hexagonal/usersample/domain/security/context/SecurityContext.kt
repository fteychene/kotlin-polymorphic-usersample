package com.fteychene.hexagonal.usersample.domain.security.context

import arrow.effects.typeclasses.MonadDefer
import com.fteychene.hexagonal.usersample.domain.app.logging.port.LoggerPort
import com.fteychene.hexagonal.usersample.domain.security.port.secondary.AclRepositoryPort
import com.fteychene.hexagonal.usersample.domain.security.port.secondary.UserRepositoryPort

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
data class SecurityContext<F>(
    val monadDefer: MonadDefer<F>,
    // App dpendencies
    val logger: LoggerPort<F>,
    // Bounded context dependencies
    val aclRepository: AclRepositoryPort<F>,
    val userRepository: UserRepositoryPort<F>
) : MonadDefer<F> by monadDefer