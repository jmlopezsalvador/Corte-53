package com.corte53.domain

/** Palo de la baraja. [code] es la letra usada en los códigos de carta y en los clips de audio. */
enum class Suit(val code: Char, val symbol: String, val plural: String, val red: Boolean) {
    CLUBS('C', "♣︎", "tréboles", false),
    HEARTS('H', "♥︎", "corazones", true),
    DIAMONDS('D', "♦︎", "diamantes", true),
    SPADES('P', "♠︎", "picas", false);

    companion object {
        fun fromCode(c: Char): Suit = entries.first { it.code == c }
    }
}

/** Valor de la carta. [code] es la letra del código (T = 10), [label] lo que se pinta. */
enum class Rank(val code: Char, val label: String, val longName: String) {
    ACE('A', "A", "As"),
    TWO('2', "2", "2"),
    THREE('3', "3", "3"),
    FOUR('4', "4", "4"),
    FIVE('5', "5", "5"),
    SIX('6', "6", "6"),
    SEVEN('7', "7", "7"),
    EIGHT('8', "8", "8"),
    NINE('9', "9", "9"),
    TEN('T', "10", "10"),
    JACK('J', "J", "Jota"),
    QUEEN('Q', "Q", "Reina"),
    KING('K', "K", "Rey");

    companion object {
        fun fromCode(c: Char): Rank = entries.first { it.code == c }
    }
}

data class Card(val rank: Rank, val suit: Suit) {
    /** Código de dos letras, p. ej. "7D" o "TC". Es también la clave del clip de audio (`c7D`). */
    val code: String get() = "${rank.code}${suit.code}"

    /** «Reina de corazones». */
    val longName: String get() = "${rank.longName} de ${suit.plural}"

    /** «Q♥». */
    val shortName: String get() = rank.label + suit.symbol

    companion object {
        fun fromCode(code: String): Card = Card(Rank.fromCode(code[0]), Suit.fromCode(code[1]))
    }
}
