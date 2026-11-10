package com.corte53.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainTest {
    @Test fun stackHas52UniqueCards() {
        assertEquals(52, MnemonicaStack.cards.size)
        assertEquals(52, MnemonicaStack.cards.toSet().size)
    }

    @Test fun positionOfInvertsCardAt() {
        for (p in 1..52) assertEquals(p, MnemonicaStack.positionOf(MnemonicaStack.cardAt(p)))
    }

    @Test fun knownPositions() {
        assertEquals("4C", MnemonicaStack.cardAt(1).code)
        assertEquals("7D", MnemonicaStack.cardAt(3).code)
        assertEquals("9D", MnemonicaStack.cardAt(52).code)
        assertEquals("Reina de corazones", MnemonicaStack.cardAt(11).longName)
    }

    @Test fun filters() {
        assertEquals(listOf(7, 9, 10), PositionFilter.positions(1, 13, CardFilter.BySuit(Suit.SPADES)))
        assertEquals(listOf(14, 22, 29, 33), PositionFilter.positions(1, 52, CardFilter.ByRank(Rank.EIGHT)))
        assertEquals(emptyList(), PositionFilter.positions(1, 5, CardFilter.ByRank(Rank.KING)))
        assertEquals((1..52).toList(), PositionFilter.positions(52, 1, CardFilter.All))
    }

    @Test fun sessionRandomHasNoRepeats() {
        val l = SessionBuilder.list((1..52).toList(), random = true, rng = Random(1))
        assertEquals((1..52).toSet(), l.toSet())
        assertEquals(52, l.size)
    }

    /** Aplica el corte a un mazo y comprueba que la carta de C acaba en D. */
    @Test fun cutsExhaustive() {
        val deck = (1..52).toList()
        for (c in 1..52) for (d in 1..52) {
            if (c == d) continue
            val cut = CutCalculator.cutFor(c, d)
            val k = cut.passedCards
            val cutDeck = deck.drop(k) + deck.take(k)   // pasa k cartas de arriba abajo
            assertEquals(c, cutDeck[d - 1], "C=$c D=$d: la carta debe acabar en D")
            if (cut.up) assertEquals(cut.n, cutDeck.first()) else assertEquals(cut.n, cutDeck.last())
        }
    }

    @Test fun glossaryExamples() {
        val a = CutCalculator.cutFor(52, 10)
        assertEquals(42, a.n); assertEquals("4D", a.refCard.code); assertEquals(false, a.up)
        val b = CutCalculator.cutFor(3, 10)
        assertEquals(46, b.n); assertEquals("QD", b.refCard.code); assertEquals(true, b.up)
    }

    @Test fun subtractions() {
        val rng = Random(7)
        repeat(500) {
            val s = SubtractionGenerator.next(SubtractionMode.DIFF, rng)
            assertTrue(s.a > s.b && s.a in 1..52 && s.b in 1..52)
            val c = SubtractionGenerator.next(SubtractionMode.COMPLEMENT, rng)
            assertEquals(53, c.a); assertTrue(c.b in 1..52); assertTrue(c.result in 1..52)
        }
    }

    @Test fun randomScenarioRespectsPool() {
        val rng = Random(3)
        repeat(200) {
            val (c, d) = CutCalculator.randomScenario(listOf(14, 22), rng)
            assertTrue(c == 14 || c == 22); assertTrue(d != c)
        }
    }

    @Test fun cutSpokenSequenceMatchesSpec() {
        val cut = CutCalculator.cutFor(3, 48)
        val keys = SpokenSequence.cutProposal(cut).clips() + "|" +
            SpokenSequence.cutExplanation(cut, 0.75).split()
        assertEquals(
            "c7D kAla n48 | kEsta n3 | n48 minus n3 kEs n45 | n53 minus n45 kEs n8 | c5H kEnla n8 | kUp",
            SpokenSequence.cutProposal(cut).clips().joinToString(" ") + " | " +
                SpokenSequence.cutExplanation(cut, 0.75).split().joinToString(" | ") { it.joinToString(" ") },
        )
        assertTrue(keys.isNotEmpty())
    }

    @Test fun listenSequences() {
        val s = SpokenSequence.listen(SpokenSequence.ListenMode.C2N, 3, 0.5, 1.5)
        assertEquals(
            listOf(Step.Visual("cardNoNumber"), Step.Clip("c7D"), Step.Pause(0.5),
                Step.Visual("showNumber"), Step.Clip("n3"), Step.Pause(1.5)),
            s,
        )
    }

    private fun List<Step>.clips(): List<String> = filterIsInstance<Step.Clip>().map { it.key }

    /** Parte la explicación en frases separadas por las pausas largas (> 0,05 s). */
    private fun List<Step>.split(): List<List<String>> {
        val out = mutableListOf<List<String>>()
        var cur = mutableListOf<String>()
        for (st in this) when (st) {
            is Step.Clip -> cur += st.key
            is Step.Pause -> if (st.seconds > 0.05) { out += cur; cur = mutableListOf() }
            is Step.Visual -> {}
        }
        if (cur.isNotEmpty()) out += cur
        return out
    }
}
