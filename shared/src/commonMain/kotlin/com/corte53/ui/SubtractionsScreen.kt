package com.corte53.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SequenceRunner
import com.corte53.audio.SpeechEngine
import com.corte53.domain.SpokenSequence
import com.corte53.domain.Step
import com.corte53.domain.SubtractionGenerator
import com.corte53.domain.SubtractionMode

@Composable
fun SubtractionsScreen(
    subs: SubtractionSettings, onSubs: (SubtractionSettings) -> Unit,
    speech: SpeechEngine, voiceId: String?, onBack: () -> Unit,
) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    var n by remember { mutableStateOf(1) }
    var sub by remember { mutableStateOf(SubtractionGenerator.next(subs.mode)) }
    var shown by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    val r by rememberUpdatedState(subs)

    KeepScreenOn(voice.on)

    fun newSub(mode: SubtractionMode = r.mode) { sub = SubtractionGenerator.next(mode); n++; shown = false }

    fun cycle() {
        voice.start {
            while (true) {
                val s = r
                val now = sub
                runner.run(
                    SpokenSequence.subtractionQuestion(now) + Step.Pause(s.think) + SpokenSequence.subtractionAnswer(now) + Step.Pause(s.gap),
                    s.rate,
                ) { if (it == "showResult") shown = true }
                newSub()
            }
        }
    }

    fun play() {
        if (voice.on) { voice.stop(); return }
        if (shown) newSub()
        cycle()
    }

    fun manual() {
        if (voice.on) return
        if (shown) newSub() else shown = true
    }

    fun skip() {
        val was = voice.on
        voice.stop(); newSub()
        if (was) cycle()
    }

    WorkScreen(
        "Restas", { voice.stop(); onBack() },
        trailing = {
            BarCounter("Nº $n")
            IconBtn(Ic.Tune, "Tiempos de la voz") { sheet = true }
        },
        controls = {
            PrimaryButton(
                if (voice.on) "Pausa" else "Cantar", Modifier.weight(1f),
                icon = if (voice.on) Ic.Pause else Ic.Play, onClick = ::play,
            )
            IconSquare(Ic.Next, "Otra resta", onClick = ::skip)
        },
    ) {
        Segmented(
            listOf(SubtractionMode.DIFF to "Entre dos números", SubtractionMode.COMPLEMENT to "Complemento a 53"), subs.mode,
            {
                val was = voice.on
                voice.stop(); onSubs(subs.copy(mode = it)); newSub(it)
                if (was) cycle()
            },
            Modifier.padding(top = 4.dp),
        )
        BoxWithConstraints(
            Modifier.fillMaxWidth().padding(top = 28.dp).panel(24)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { manual() }
                .padding(horizontal = 12.dp, vertical = 36.dp),
        ) {
            val size = (maxWidth.value * 0.13f).coerceIn(41.6f, 67.2f).sp
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sub.a.toString(), color = Pal.Cream, fontFamily = Serif, fontSize = size)
                    Text("−", color = Pal.CreamDim, fontFamily = Serif, fontSize = size, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(sub.b.toString(), color = Pal.Cream, fontFamily = Serif, fontSize = size)
                    Text("=", color = Pal.CreamDim, fontFamily = Serif, fontSize = size, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        if (shown) sub.result.toString() else "?", fontFamily = Serif, fontSize = size,
                        color = if (shown) Pal.Salmon else Pal.Line, textAlign = TextAlign.Center,
                        modifier = Modifier.width((size.value * 1.6f).dp),
                    )
                }
                Text(
                    if (voice.on) "" else if (shown) "Toca para la siguiente" else "Toca para ver el resultado",
                    color = Pal.CreamDim, fontSize = 14.sp, modifier = Modifier.padding(top = 14.dp),
                )
            }
        }
        VoiceWarning(speech, voiceId)
        voice.error?.let { Warning(it) }
    }

    if (sheet) Sheet("Tiempos de la voz", { sheet = false }) {
        LabeledSlider("Pausa hasta el resultado", subs.think, "${dec(subs.think)} s", 0.0, 8.0, 0.5) { onSubs(subs.copy(think = it)) }
        LabeledSlider("Pausa hasta la siguiente", subs.gap, "${dec(subs.gap)} s", 0.5, 6.0, 0.5) { onSubs(subs.copy(gap = it)) }
        LabeledSlider("Velocidad de la voz", subs.rate, "${dec(subs.rate)}×", 0.6, 1.4, 0.1) { onSubs(subs.copy(rate = it)) }
    }
}
