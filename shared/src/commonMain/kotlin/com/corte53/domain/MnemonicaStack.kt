package com.corte53.domain

/** El orden Mnemónica de Juan Tamariz. Posición 1 = carta superior del mazo boca abajo. */
object MnemonicaStack {
    const val SIZE = 52

    private val CODES = listOf(
        "4C", "2H", "7D", "3C", "4H", "6D", "AP", "5H", "9P", "2P", "QH", "3D", "QC",
        "8H", "6P", "5P", "9H", "KC", "2D", "JH", "3P", "8P", "6H", "TC", "5D", "KD",
        "2C", "3H", "8D", "5C", "KP", "JD", "8C", "TP", "KH", "JC", "7P", "TH", "AD", "4P",
        "7H", "4D", "AC", "9C", "JP", "QD", "7C", "QP", "TD", "6C", "AH", "9D",
    )

    val cards: List<Card> = CODES.map { Card.fromCode(it) }

    private val positions: Map<Card, Int> = cards.withIndex().associate { (i, c) -> c to i + 1 }

    /** Carta en la posición [position] (1..52). */
    fun cardAt(position: Int): Card = cards[position - 1]

    /** Posición (1..52) de [card]. */
    fun positionOf(card: Card): Int = positions.getValue(card)
}
