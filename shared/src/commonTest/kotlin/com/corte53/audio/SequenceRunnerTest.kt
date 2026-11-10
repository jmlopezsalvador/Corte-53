package com.corte53.audio

import com.corte53.domain.CutCalculator
import com.corte53.domain.SpokenSequence
import com.corte53.domain.Step
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeEngine : SpeechEngine {
    val said = mutableListOf<String>()
    var stopped = 0
    override val voices: List<SpeechVoice> = emptyList()
    override fun setVoice(id: String?) {}
    override suspend fun speak(text: String, rate: Double) {
        said += text
        delay(500)
    }
    override fun stop() { stopped++ }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SequenceRunnerTest {
    @Test fun groupsTightClipsIntoPhrases() = runTest {
        val engine = FakeEngine()
        val cut = CutCalculator.cutFor(3, 48)
        SequenceRunner(engine).run(SpokenSequence.cutFull(cut, think = 5.0, step = 0.75), 1.0)
        assertEquals(
            listOf(
                "siete de diamantes a la posición cuarenta y ocho",
                "está en la tres",
                "cuarenta y ocho menos tres es cuarenta y cinco",
                "cincuenta y tres menos cuarenta y cinco es ocho",
                "cinco de corazones en la ocho",
                "cortar arriba",
            ),
            engine.said,
        )
    }

    @Test fun honoursPausesAndVisualEvents() = runTest {
        val engine = FakeEngine()
        val events = mutableListOf<Pair<String, Long>>()
        val steps = listOf(
            Step.Visual("a"), Step.Clip("n1"), Step.Pause(2.0), Step.Visual("b"), Step.Clip("n2"),
        )
        SequenceRunner(engine).run(steps, 1.0) { events += it to currentTime }
        assertEquals(listOf("a" to 0L, "b" to 2500L), events)   // 500 ms de voz + 2 s de pausa
        assertEquals(listOf("uno", "dos"), engine.said)
    }

    @Test fun cancellationStopsPendingSteps() = runTest {
        val engine = FakeEngine()
        val job = async(start = CoroutineStart.UNDISPATCHED) {
            SequenceRunner(engine).run(listOf(Step.Clip("n1"), Step.Pause(10.0), Step.Clip("n2")), 1.0)
        }
        advanceTimeBy(1000)
        job.cancel()
        advanceTimeBy(20_000)
        assertEquals(listOf("uno"), engine.said)
        assertTrue(job.isCancelled)
    }

    @Test fun everyClipHasText() {
        for (p in 1..53) ClipTexts.text("n$p")
        for (card in com.corte53.domain.MnemonicaStack.cards) ClipTexts.text("c${card.code}")
        listOf("minus", "kAla", "kEsta", "kEs", "kEnla", "kUp", "kDown").forEach { ClipTexts.text(it) }
    }
}
