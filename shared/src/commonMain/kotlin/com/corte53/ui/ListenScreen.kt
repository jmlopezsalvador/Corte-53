package com.corte53.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.corte53.audio.SequenceRunner
import com.corte53.audio.SpeechEngine
import com.corte53.domain.PositionFilter
import com.corte53.domain.SessionBuilder
import com.corte53.domain.SpokenSequence
import com.corte53.domain.SpokenSequence.ListenMode

private val MODES = listOf(
    ListenMode.SAY to "Recitar", ListenMode.N2C to "Nº → carta", ListenMode.C2N to "Carta → nº",
    ListenMode.ONLY_NUMBERS to "Solo números", ListenMode.ONLY_CARDS to "Solo cartas",
)

@Composable
fun ListenScreen(
    menu: MenuSettings, audio: AudioSettings, onAudio: (AudioSettings) -> Unit,
    speech: SpeechEngine, voiceId: String?, onBack: () -> Unit,
) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    fun build() = SessionBuilder.list(PositionFilter.positions(menu.from, menu.to, menu.filter), menu.random)
    var list by remember { mutableStateOf(build()) }
    var i by remember { mutableStateOf(0) }
    var visual by remember { mutableStateOf("cardWithNumber") }
    var emptyNotice by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    val a by rememberUpdatedState(audio)
    val mode = ListenMode.entries.firstOrNull { it.name == audio.mode } ?: ListenMode.SAY

    KeepScreenOn(voice.on)

    fun reset() {
        voice.stop()
        list = build(); i = 0; visual = "cardWithNumber"
        emptyNotice = list.isEmpty()
    }

    fun play() {
        emptyNotice = false
        if (voice.on) { voice.stop(); return }
        if (list.isEmpty() || i >= list.size) { list = build(); i = 0 }
        if (list.isEmpty()) { emptyNotice = true; return }
        voice.start {
            while (true) {
                if (i >= list.size) {
                    if (a.loop) { list = build(); i = 0 } else break
                    if (list.isEmpty()) break
                }
                val s = a
                runner.run(
                    SpokenSequence.listen(
                        ListenMode.entries.firstOrNull { it.name == s.mode } ?: ListenMode.SAY, list[i], s.inner, s.gap,
                    ),
                    s.rate,
                ) { visual = it }
                i++
            }
        }
    }

    val finished = !voice.on && list.isNotEmpty() && i >= list.size
    WorkScreen(
        "Escuchar", { voice.stop(); onBack() },
        trailing = {
            BarCounter("${minOf(i + 1, list.size)} / ${list.size}")
            IconBtn(Ic.Tune, "Ajustes de escucha") { sheet = true }
        },
        scroll = false,
        controls = {
            IconSquare(Ic.Reset, "Volver al principio", onClick = ::reset)
            PrimaryButton(
                if (voice.on) "Pausa" else if (finished) "Otra vez" else if (i > 0) "Continuar" else "Reproducir",
                Modifier.weight(1f), icon = if (voice.on) Ic.Pause else Ic.Play, onClick = ::play,
            )
        },
    ) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val w = minOf(maxWidth * 0.58f, 240.dp, (maxHeight - 24.dp) / 1.4f)
            val pos = list.getOrNull(minOf(i, list.size - 1))
            if (audio.show && pos != null) {
                if (visual == "backWithNumber") CardBack(pos, w) else BigCard(pos, w, badgeVisible = visual != "cardNoNumber")
            } else if (!audio.show) {
                Icon(Ic.Speaker, Pal.Line, 96.dp)
            } else Box(Modifier.size(w, w * 1.4f))
        }
        Column(Modifier.padding(bottom = 4.dp)) {
            ChipRow { MODES.forEach { (m, label) -> Chip(label, mode == m) { voice.stop(); onAudio(audio.copy(mode = m.name)) } } }
            Hint(
                when (mode) {
                    ListenMode.SAY -> "Dice el número y la carta seguidos."
                    ListenMode.N2C -> "Dice el número y, tras la pausa, la carta."
                    ListenMode.C2N -> "Dice la carta y, tras la pausa, el número."
                    ListenMode.ONLY_NUMBERS -> "Lee solo los números. Tú dices la carta en la pausa."
                    ListenMode.ONLY_CARDS -> "Lee solo las cartas. Tú dices el número en la pausa."
                },
            )
            VoiceWarning(speech, voiceId)
            voice.error?.let { Warning(it) }
            if (emptyNotice) Warning("No hay cartas con ese filtro en el rango elegido. Cámbialo en Memorizar.")
        }
    }

    if (sheet) Sheet("Ajustes de escucha", { sheet = false }) {
        if (mode != ListenMode.ONLY_NUMBERS && mode != ListenMode.ONLY_CARDS) {
            LabeledSlider("Pausa entre número y carta", audio.inner, "${dec(audio.inner)} s", 0.0, 5.0, 0.1) { onAudio(audio.copy(inner = it)) }
        }
        LabeledSlider("Pausa entre cartas", audio.gap, "${dec(audio.gap)} s", 0.5, 6.0, 0.5) { onAudio(audio.copy(gap = it)) }
        LabeledSlider("Velocidad de la voz", audio.rate, "${dec(audio.rate)}×", 0.6, 1.4, 0.1) { onAudio(audio.copy(rate = it)) }
        Toggle("Repetir al terminar", audio.loop) { onAudio(audio.copy(loop = it)) }
        Toggle("Mostrar la carta mientras suena", audio.show) { onAudio(audio.copy(show = it)) }
    }
}
