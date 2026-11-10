package com.corte53.audio

import com.corte53.domain.Step
import kotlinx.coroutines.delay

/**
 * Ejecuta una lista de [Step] con un [SpeechEngine]. Único motor para Escuchar, Cortes y Restas.
 *
 * - Los clips seguidos con pausas cortas (≤ [PHRASE_GAP]) se juntan en UNA frase hablada, para que
 *   la entonación sea natural («cuarenta y ocho menos tres es cuarenta y cinco»).
 * - Las pausas largas son esperas reales; los [Step.Visual] llaman a [onVisual].
 * - Cancelar la corrutina que lo ejecuta corta la voz y las esperas (cancelación estructurada).
 */
class SequenceRunner(
    private val engine: SpeechEngine,
    private val textOf: (String) -> String = ClipTexts::text,
) {
    suspend fun run(steps: List<Step>, rate: Double, onVisual: (String) -> Unit = {}) {
        val phrase = mutableListOf<String>()

        suspend fun flush() {
            if (phrase.isEmpty()) return
            val text = phrase.joinToString(" ")
            phrase.clear()
            engine.speak(text, rate)
        }

        for (step in steps) {
            when (step) {
                is Step.Clip -> phrase += textOf(step.key)
                is Step.Pause -> if (step.seconds > PHRASE_GAP) {
                    flush()
                    delay((step.seconds * 1000).toLong())
                }
                is Step.Visual -> {
                    flush()
                    onVisual(step.event)
                }
            }
        }
        flush()
    }

    companion object {
        /** Pausas de este tamaño o menores no cortan la frase. */
        const val PHRASE_GAP = 0.05
    }
}
