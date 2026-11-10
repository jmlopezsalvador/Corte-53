package com.corte53.domain

/** Filtro de cartas del menú: todas, un palo o un valor. */
sealed interface CardFilter {
    data object All : CardFilter
    data class BySuit(val suit: Suit) : CardFilter
    data class ByRank(val rank: Rank) : CardFilter
}

object PositionFilter {
    /** Posiciones de [from]..[to] (se intercambian si vienen al revés) que pasan el [filter]. */
    fun positions(from: Int, to: Int, filter: CardFilter): List<Int> {
        val lo = minOf(from, to).coerceIn(1, MnemonicaStack.SIZE)
        val hi = maxOf(from, to).coerceIn(1, MnemonicaStack.SIZE)
        return (lo..hi).filter { p ->
            val c = MnemonicaStack.cardAt(p)
            when (filter) {
                CardFilter.All -> true
                is CardFilter.BySuit -> c.suit == filter.suit
                is CardFilter.ByRank -> c.rank == filter.rank
            }
        }
    }
}
