package com.corte53.domain

import kotlin.random.Random

/**
 * Resultado de «carta al número».
 * [c] posición actual, [dest] destino (D), [n] posición de la carta de referencia;
 * [up] true = esa carta queda arriba del todo (corte arriba), false = abajo del todo;
 * [d] diferencia usada (D − C si baja, C − D si sube).
 */
data class Cut(val c: Int, val dest: Int, val n: Int, val up: Boolean, val d: Int) {
    /** Carta de referencia (la de la posición N). */
    val refCard: Card get() = MnemonicaStack.cardAt(n)

    /** Cartas que se pasan de arriba abajo: N − 1 si queda arriba, N si queda abajo. */
    val passedCards: Int get() = if (up) n - 1 else n
}

object CutCalculator {
    /** C = posición actual, D = destino (C ≠ D). Única fuente de verdad de las fórmulas. */
    fun cutFor(c: Int, d: Int): Cut {
        require(c != d) { "C y D deben ser distintas" }
        require(c in 1..52 && d in 1..52)
        return if (d > c) {
            val diff = d - c
            Cut(c, d, n = 53 - diff, up = true, d = diff)
        } else {
            Cut(c, d, n = c - d, up = false, d = c - d)
        }
    }

    /** Escenario al azar: C sale de [positions] (o de las 52 si está vacía); D distinta de C. */
    fun randomScenario(positions: List<Int>, rng: Random = Random): Pair<Int, Int> {
        val pool = positions.ifEmpty { (1..52).toList() }
        val c = pool.random(rng)
        var d: Int
        do { d = rng.nextInt(1, 53) } while (d == c)
        return c to d
    }
}
