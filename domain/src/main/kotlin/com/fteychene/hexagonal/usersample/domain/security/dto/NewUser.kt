package com.fteychene.hexagonal.usersample.domain.security.dto

import arrow.core.None
import arrow.core.Option
import java.util.*

data class NewUser(
    val name: String,
    val groups: Option<List<UUID>> = None
)