package com.fteychene.hexagonal.usersample.domain.security.port.secondary

import arrow.data.EitherT
import com.fteychene.hexagonal.usersample.domain.security.dto.NewGroup
import com.fteychene.hexagonal.usersample.domain.security.model.Group
import com.fteychene.hexagonal.usersample.domain.security.model.SecurityError

interface AclRepositoryPort<F> {

    fun registerGroup(group: NewGroup): EitherT<F, SecurityError, Unit>

    fun groupExists(groups: List<String>): EitherT<F, SecurityError, Boolean>
}