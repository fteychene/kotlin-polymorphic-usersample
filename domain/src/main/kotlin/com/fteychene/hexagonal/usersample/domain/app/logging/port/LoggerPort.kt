package com.fteychene.hexagonal.usersample.domain.app.logging.port

import arrow.Kind

interface LoggerPort <F> {

    fun log(message: () -> String): Kind<F, Unit>

}