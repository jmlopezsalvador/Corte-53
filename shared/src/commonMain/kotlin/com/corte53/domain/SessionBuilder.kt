package com.corte53.domain

import kotlin.random.Random

enum class Direction { NUMBER_TO_CARD, CARD_TO_NUMBER }

/** «Qué te enseño»: te muestro el número, la carta, o al azar. */
enum class TeachMode { NUMBER, CARD, MIXED }

object SessionBuilder {
    /** Lista de la sesión: secuencial o barajada (sin repetición). */
    fun list(positions: List<Int>, random: Boolean, rng: Random = Random): List<Int> =
        if (random) positions.shuffled(rng) else positions

    /**
     * Qué se pregunta en cada carta. En Mixto se sortea 50 % al crear la sesión; en los otros dos
     * modos es siempre el mismo.
     */
    fun directions(count: Int, mode: TeachMode, rng: Random = Random): List<Direction> = List(count) {
        when (mode) {
            TeachMode.NUMBER -> Direction.NUMBER_TO_CARD
            TeachMode.CARD -> Direction.CARD_TO_NUMBER
            TeachMode.MIXED -> if (rng.nextBoolean()) Direction.NUMBER_TO_CARD else Direction.CARD_TO_NUMBER
        }
    }
}
