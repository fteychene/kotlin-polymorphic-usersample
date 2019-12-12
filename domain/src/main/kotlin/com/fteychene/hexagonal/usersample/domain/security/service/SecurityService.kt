package com.fteychene.hexagonal.usersample.domain.security.service

import arrow.Kind
import arrow.core.extensions.option.traverse.traverse
import arrow.core.fix
import arrow.core.getOrElse
import arrow.data.EitherT
import arrow.data.extensions.applicativeError
import arrow.data.extensions.eithert.applicative.applicative
import arrow.data.extensions.eithert.monad.binding
import arrow.data.fix
import com.fteychene.hexagonal.usersample.domain.security.context.SecurityContext
import com.fteychene.hexagonal.usersample.domain.security.dto.NewGroup
import com.fteychene.hexagonal.usersample.domain.security.dto.NewUser
import com.fteychene.hexagonal.usersample.domain.security.model.Group
import com.fteychene.hexagonal.usersample.domain.security.model.InvalidArgument
import com.fteychene.hexagonal.usersample.domain.security.model.SecurityError
import com.fteychene.hexagonal.usersample.domain.security.model.User
import com.fteychene.hexagonal.usersample.domain.security.port.primary.SecurityPort
import com.fteychene.hexagonal.usersample.domain.security.port.primary.SecurityResult


class SecurityService<F>(
    private val ctx: SecurityContext<F>
) : SecurityPort<F> {

    internal fun <E, T> raiseError(error: E): EitherT<F, E, T> =
        EitherT.applicativeError<F, E>(ctx.monadDefer).raiseError<T>(error).fix()

    internal fun <T> raiseError(error: SecurityError): EitherT<F, SecurityError, T> =
        raiseError<SecurityError, T>(error)

    internal fun <E, T> contextualize(value: Kind<F, T>): EitherT<F, E, T> =
        EitherT.liftF(ctx.monadDefer, value)

    override fun registerNewUser(user: NewUser): SecurityResult<F, User> = binding(ctx.monadDefer) {
        // Load the groups defined in the new user to check if they exists
        contextualize<SecurityError, Unit>(ctx.logger.log { "Loading groups from database" }).bind()
        val groups = user.groups.traverse(EitherT.applicative<F, SecurityError>(ctx.monadDefer)) { groups -> ctx.userRepository.findGroups(groups)}.bind().fix()
        if (groups.map { it.size } != user.groups.map { it.size }) {
            raiseError<User>(
                InvalidArgument(
                    "All groups defined in the user are not existing in our database"
                )
            ).bind()
        }
        contextualize<SecurityError, Unit>(ctx.logger.log { "Checking groups from acl repository" }).bind()
        val groupsInAcl = ctx.aclRepository.groupExists(groups.getOrElse { listOf() }.map { it.name }).bind()
        if (!groupsInAcl) {
            raiseError<User>(
                InvalidArgument(
                    "All groups defined in the user does not exist in the acl repository"
                )
            ).bind()
        }
        contextualize<SecurityError, Unit>(ctx.logger.log { "Create user $user" }).bind()
        ctx.userRepository.insertUser(user).bind()
    }

    override fun registerNewGroup(group: NewGroup): EitherT<F, SecurityError, Group> = binding(ctx.monadDefer) {
        contextualize<SecurityError, Unit>(ctx.logger.log { "Register new group in acl repository" }).bind()
        ctx.aclRepository.registerGroup(group).bind()
        contextualize<SecurityError, Unit>(ctx.logger.log { "Create new group in user repository $group" }).bind()
        ctx.userRepository.insertGroup(group).bind()
    }

}