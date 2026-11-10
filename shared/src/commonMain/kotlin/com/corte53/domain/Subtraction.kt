package com.corte53.domain

import kotlin.random.Random

enum class SubtractionMode { DIFF, COMPLEMENT }

/** [a] − [b] = [result]. */
data class Subtraction(val a: Int, val b: Int) {
    val result: Int get() = a - b
}

object SubtractionGenerator {
    fun next(mode: SubtractionMode, rng: Random = Random): Subtraction = when (mode) {
        SubtractionMode.DIFF -> {
            val x = rng.nextInt(1, 53)
            var y: Int
            do { y = rng.nextInt(1, 53) } while (y == x)
            Subtraction(maxOf(x, y), minOf(x, y))
        }
        SubtractionMode.COMPLEMENT -> Subtraction(53, rng.nextInt(1, 53))
    }
}
