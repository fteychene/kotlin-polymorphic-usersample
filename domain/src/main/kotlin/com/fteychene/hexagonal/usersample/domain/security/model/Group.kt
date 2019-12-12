package com.fteychene.hexagonal.usersample.domain.security.model

import arrow.core.Eval
import java.util.*

data class Group(
    val id: UUID,
    val name: String,
    val users: Eval<List<User>>
)