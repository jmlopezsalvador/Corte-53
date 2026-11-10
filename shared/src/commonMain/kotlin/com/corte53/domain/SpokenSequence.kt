package com.corte53.domain

/** Paso de una locución: reproducir un clip, esperar, o cambiar lo que se ve en pantalla. */
sealed interface Step {
    data class Clip(val key: String) : Step
    data class Pause(val seconds: Double) : Step
    data class Visual(val event: String) : Step
}

/** Generadores de la lista de pasos de cada modo (sin audio: se testean como datos). */
object SpokenSequence {
    private const val TIGHT = 0.05   // entre clips de una misma frase

    fun n(x: Int) = Step.Clip("n$x")
    fun c(card: Card) = Step.Clip("c${card.code}")

    // ---- Escuchar en audio ----
    enum class ListenMode { SAY, N2C, C2N, ONLY_NUMBERS, ONLY_CARDS }

    fun listen(mode: ListenMode, position: Int, inner: Double, gap: Double): List<Step> {
        val card = MnemonicaStack.cardAt(position)
        return when (mode) {
            ListenMode.SAY -> listOf(
                Step.Visual("cardWithNumber"), n(position), Step.Pause(inner), c(card), Step.Pause(gap),
            )
            ListenMode.N2C -> listOf(
                Step.Visual("backWithNumber"), n(position), Step.Pause(inner),
                Step.Visual("cardWithNumber"), c(card), Step.Pause(gap),
            )
            ListenMode.C2N -> listOf(
                Step.Visual("cardNoNumber"), c(card), Step.Pause(inner),
                Step.Visual("showNumber"), n(position), Step.Pause(gap),
            )
            ListenMode.ONLY_NUMBERS -> listOf(Step.Visual("backWithNumber"), n(position), Step.Pause(gap))
            ListenMode.ONLY_CARDS -> listOf(Step.Visual("cardNoNumber"), c(card), Step.Pause(gap))
        }
    }

    // ---- Cortes con voz ----
    fun cutProposal(cut: Cut): List<Step> =
        tight(c(MnemonicaStack.cardAt(cut.c)), Step.Clip("kAla"), n(cut.dest))

    fun cutExplanation(cut: Cut, step: Double): List<Step> {
        val s = mutableListOf<Step>()
        s += tight(Step.Clip("kEsta"), n(cut.c)); s += Step.Pause(step)
        if (cut.up) {
            s += tight(n(cut.dest), Step.Clip("minus"), n(cut.c), Step.Clip("kEs"), n(cut.d)); s += Step.Pause(step)
            s += tight(n(53), Step.Clip("minus"), n(cut.d), Step.Clip("kEs"), n(cut.n)); s += Step.Pause(step)
        } else {
            s += tight(n(cut.c), Step.Clip("minus"), n(cut.dest), Step.Clip("kEs"), n(cut.n)); s += Step.Pause(step)
        }
        s += tight(c(cut.refCard), Step.Clip("kEnla"), n(cut.n)); s += Step.Pause(step)
        s += Step.Clip(if (cut.up) "kUp" else "kDown")
        return s
    }

    /** Propuesta, pausa para pensar, revelación y explicación. */
    fun cutFull(cut: Cut, think: Double, step: Double): List<Step> =
        cutProposal(cut) + Step.Pause(think) + Step.Visual("reveal") + cutExplanation(cut, step)

    // ---- Restas con voz ----
    fun subtractionQuestion(s: Subtraction): List<Step> =
        tight(n(s.a), Step.Clip("minus"), n(s.b))

    fun subtractionAnswer(s: Subtraction): List<Step> = listOf(Step.Visual("showResult"), n(s.result))

    private fun tight(vararg clips: Step.Clip): List<Step> {
        val out = mutableListOf<Step>()
        clips.forEachIndexed { i, c ->
            if (i > 0) out += Step.Pause(TIGHT)
            out += c
        }
        return out
    }
}
