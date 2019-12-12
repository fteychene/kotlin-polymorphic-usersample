package com.fteychene.hexagonal.usersample.domain.security.port.secondary

import arrow.data.EitherT
import com.fteychene.hexagonal.usersample.domain.security.dto.NewGroup
import com.fteychene.hexagonal.usersample.domain.security.dto.NewUser
import com.fteychene.hexagonal.usersample.domain.security.model.Group
import com.fteychene.hexagonal.usersample.domain.security.model.SecurityError
import com.fteychene.hexagonal.usersample.domain.security.model.User
import java.util.*

interface UserRepositoryPort<F> {

    fun insertGroup(group: NewGroup): EitherT<F, SecurityError, Group>

    fun insertUser(newUser: NewUser): EitherT<F, SecurityError, User>

    fun findGroups(groups: List<UUID>): EitherT<F, SecurityError, List<Group>>
}