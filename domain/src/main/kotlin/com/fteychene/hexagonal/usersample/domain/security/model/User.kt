package com.fteychene.hexagonal.usersample.domain.security.model

import arrow.core.Eval
import java.util.*

data class User(
    val id: UUID,
    val username: String,
    val groups: Eval<List<Group>>
)