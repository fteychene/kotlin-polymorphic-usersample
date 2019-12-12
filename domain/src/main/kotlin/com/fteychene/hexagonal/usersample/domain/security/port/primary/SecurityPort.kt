package com.fteychene.hexagonal.usersample.domain.security.port.primary

import arrow.data.EitherT
import com.fteychene.hexagonal.usersample.domain.security.dto.NewGroup
import com.fteychene.hexagonal.usersample.domain.security.dto.NewUser
import com.fteychene.hexagonal.usersample.domain.security.model.SecurityError
import com.fteychene.hexagonal.usersample.domain.security.model.Group
import com.fteychene.hexagonal.usersample.domain.security.model.User

typealias SecurityResult<F, T> = EitherT<F, SecurityError, T>

interface SecurityPort<F> {

    fun registerNewGroup(group: NewGroup): SecurityResult<F, Group>

    fun registerNewUser(user: NewUser): SecurityResult<F, User>

}