package com.fteychene.hexagonal.usersample.domain.security.service

import arrow.Kind
import arrow.core.*
import arrow.core.extensions.EitherMonadError
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monadError.monadError
import arrow.data.EitherT
import arrow.effects.typeclasses.ExitCase
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.MonadError
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import com.fteychene.hexagonal.usersample.domain.app.logging.port.LoggerPort
import com.fteychene.hexagonal.usersample.domain.security.context.SecurityContext
import com.fteychene.hexagonal.usersample.domain.security.dto.NewGroup
import com.fteychene.hexagonal.usersample.domain.security.model.Group
import com.fteychene.hexagonal.usersample.domain.security.model.User
import com.fteychene.hexagonal.usersample.domain.security.port.secondary.AclRepositoryPort
import com.fteychene.hexagonal.usersample.domain.security.port.secondary.UserRepositoryPort
import org.junit.jupiter.api.Test
import java.util.*

class EitherMonadDefer(private val monad: EitherMonadError<Throwable>): MonadError<EitherPartialOf<Throwable>, Throwable> by monad, MonadDefer<EitherPartialOf<Throwable>> {
    override fun <A> defer(fa: () -> Kind<EitherPartialOf<Throwable>, A>): Kind<EitherPartialOf<Throwable>, A> = fa()

    override fun <A, B> Kind<EitherPartialOf<Throwable>, A>.bracketCase(
        release: (A, ExitCase<Throwable>) -> Kind<EitherPartialOf<Throwable>, Unit>,
        use: (A) -> Kind<EitherPartialOf<Throwable>, B>
    ): Kind<EitherPartialOf<Throwable>, B> = flatMap(use)

}

internal class TestLogger:
    LoggerPort<EitherPartialOf<Throwable>> {

    var acc = listOf<String>()

    override fun log(message: () -> String): Kind<EitherPartialOf<Throwable>, Unit> {
        acc = acc + message()
        return Unit.right()
    }

}


internal class SecurityServiceTest {

    @Test
    fun `registerNewGroup should fail if effect fail`() {
        val aclRepository = mockk<AclRepositoryPort<EitherPartialOf<Throwable>>>()
        val context = SecurityContext(
            EitherMonadDefer(Either.monadError()),
            TestLogger(),
            aclRepository,
            mockk()
        )
        val service = SecurityService(context)

        val error = IllegalStateException("Runtime async error")
        every { aclRepository.registerGroup(NewGroup("testGroup")) } returns EitherT(error.left())

        val test = service.registerNewGroup(NewGroup("testGroup")).value().fix()
        test.shouldBeLeft(error)
    }

    @Test
    fun `registerNewGroup test logs`() {
        val aclRepository = mockk<AclRepositoryPort<EitherPartialOf<Throwable>>>()
        val userRepository = mockk<UserRepositoryPort<EitherPartialOf<Throwable>>>()
        val logger = TestLogger()
        val context = SecurityContext(
            EitherMonadDefer(Either.monadError()),
            logger,
            aclRepository,
            userRepository
        )
        val service = SecurityService(context)

        val result = Group(
            id = UUID.randomUUID(),
            name = "testGroup",
            users = Eval.always { listOf<User>() })
        every { aclRepository.registerGroup(NewGroup("testGroup")) } returns EitherT.just(Either.applicative(), Unit)
        every { userRepository.insertGroup(NewGroup("testGroup")) } returns EitherT.just(Either.applicative(), result)

        val test = service.registerNewGroup(NewGroup("testGroup")).value().fix()
        test.shouldBeRight(result.right())
        logger.acc shouldBe listOf("Register new group in acl repository", "Create new group in user repository ${NewGroup(
            "testGroup"
        )}")
    }
}