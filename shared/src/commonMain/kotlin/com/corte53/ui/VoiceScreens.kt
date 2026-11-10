package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.corte53.audio.SequenceRunner
import com.corte53.audio.SpeechEngine
import com.corte53.audio.SpeechException
import com.corte53.domain.CutCalculator
import com.corte53.domain.MnemonicaStack
import com.corte53.domain.PositionFilter
import com.corte53.domain.SessionBuilder
import com.corte53.domain.SpokenSequence
import com.corte53.domain.SpokenSequence.ListenMode
import com.corte53.domain.Step
import com.corte53.domain.SubtractionGenerator
import com.corte53.domain.SubtractionMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Lanza y para la locución de una pantalla. Un solo trabajo a la vez; parar lo cancela
 * (la voz se corta sola por la cancelación estructurada) y avisa si el motor falla.
 */
class VoiceController(private val scope: CoroutineScope) {
    var on by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var job: Job? = null

    fun start(block: suspend () -> Unit) {
        job?.cancel()
        error = null
        on = true
        val me = scope.launch(start = CoroutineStart.LAZY) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SpeechException) {
                error = e.message ?: "No se pudo reproducir la voz"
            } finally {
                if (job === currentCoroutineContext()[Job]) on = false
            }
        }
        job = me
        me.start()
    }

    fun stop() {
        job?.cancel()
        job = null
        on = false
    }
}

@Composable
private fun rememberVoice(): VoiceController {
    val scope = rememberCoroutineScope()
    return remember { VoiceController(scope) }
}

@Composable
private fun VoiceNotice(text: String?) {
    if (text != null) Hint(text)
}

/** Botón principal + botón cuadrado de 64 dp (↺ o ›). */
@Composable
private fun ControlRow(primary: String, onPrimary: () -> Unit, small: String?, onSmall: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PrimaryButton(primary, Modifier.weight(1f), onClick = onPrimary)
        if (small != null) GhostButton(small, Modifier.width(64.dp), fontSize = 20.8f, onClick = onSmall)
    }
}

// =====================================================================================================
// Escuchar en audio
// =====================================================================================================

@Composable
fun ListenScreen(
    menu: MenuSettings, audio: AudioSettings, onAudio: (AudioSettings) -> Unit,
    speech: SpeechEngine, onBack: () -> Unit,
) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    fun build() = SessionBuilder.list(PositionFilter.positions(menu.from, menu.to, menu.filter), menu.random)
    var list by remember { mutableStateOf(build()) }
    var i by remember { mutableStateOf(0) }
    var visual by remember { mutableStateOf("cardWithNumber") }
    var emptyNotice by remember { mutableStateOf(false) }
    val a by rememberUpdatedState(audio)
    val mode = ListenMode.entries.firstOrNull { it.name == audio.mode } ?: ListenMode.SAY

    PlatformBackHandler(true) { voice.stop(); onBack() }
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

    Screen {
        TopBar(
            "‹ Volver", { voice.stop(); onBack() }, center = { BarTitle("Escuchar") },
            right = { Text("${minOf(i + 1, list.size)} / ${list.size}", color = Pal.Ink, fontWeight = FontWeight.Bold) },
        )
        if (audio.show) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 14.dp), contentAlignment = Alignment.Center) {
                val w = minOf(maxWidth * 0.46f, 180.dp)
                val pos = list.getOrNull(minOf(i, list.size - 1))
                if (pos != null) {
                    if (visual == "backWithNumber") CardBack(pos, w)
                    else BigCard(pos, w, badgeVisible = visual != "cardNoNumber")
                } else Box(Modifier.size(w, w * 1.4f))
            }
        }
        Hint(
            "Posiciones ${menu.from}–${menu.to} · ${if (menu.random) "aleatorio" else "secuencial"}" +
                (if (menu.filterKind != FilterKind.ALL) " · ${filterLabel(menu)}" else ""),
            center = true, modifier = Modifier.padding(bottom = 14.dp),
        )
        val finished = !voice.on && list.isNotEmpty() && i >= list.size
        ControlRow(
            if (voice.on) "❚❚ Pausa" else if (finished) "▶ Otra vez" else if (i > 0) "▶ Continuar" else "▶ Reproducir",
            ::play, "↺", ::reset,
        )
        VoiceNotice(voice.error ?: if (emptyNotice) "No hay cartas con ese filtro en el rango elegido. Cámbialo en el menú." else null)

        Label("Modo")
        Segmented(
            listOf(
                ListenMode.SAY to "Recitar", ListenMode.N2C to "Nº → carta", ListenMode.C2N to "Carta → nº",
                ListenMode.ONLY_NUMBERS to "Solo números", ListenMode.ONLY_CARDS to "Solo cartas",
            ),
            mode, { voice.stop(); onAudio(audio.copy(mode = it.name)) }, perRow = 3,
        )
        Hint(
            when (mode) {
                ListenMode.SAY -> "Dice el número y la carta seguidos: «uno, cuatro de tréboles»."
                ListenMode.N2C -> "Dice el número y, tras la pausa, la carta."
                ListenMode.C2N -> "Dice la carta y, tras la pausa, el número."
                ListenMode.ONLY_NUMBERS -> "Lee solo los números. Tú dices la carta en la pausa."
                ListenMode.ONLY_CARDS -> "Lee solo las cartas. Tú dices el número en la pausa."
            },
        )
        if (mode != ListenMode.ONLY_NUMBERS && mode != ListenMode.ONLY_CARDS) {
            LabeledSlider("Pausa entre número y carta", audio.inner, "${dec(audio.inner)} s", 0.0, 5.0, 0.1) { onAudio(audio.copy(inner = it)) }
        }
        LabeledSlider("Pausa entre cartas", audio.gap, "${dec(audio.gap)} s", 0.5, 6.0, 0.5) { onAudio(audio.copy(gap = it)) }
        LabeledSlider("Velocidad de la voz", audio.rate, "${dec(audio.rate)}×", 0.6, 1.4, 0.1) { onAudio(audio.copy(rate = it)) }
        Toggle("Repetir al terminar", audio.loop) { onAudio(audio.copy(loop = it)) }
        Toggle("Mostrar la carta mientras suena", audio.show) { onAudio(audio.copy(show = it)) }
    }
}

// =====================================================================================================
// Entrenar cortes
// =====================================================================================================

@Composable
fun CutsScreen(
    menu: MenuSettings, cuts: CutSettings, onCuts: (CutSettings) -> Unit,
    speech: SpeechEngine, onBack: () -> Unit,
) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    val pool = remember(menu) { PositionFilter.positions(menu.from, menu.to, menu.filter) }
    var n by remember { mutableStateOf(1) }
    var scenario by remember { mutableStateOf(CutCalculator.randomScenario(pool)) }
    var shown by remember { mutableStateOf(false) }
    val k by rememberUpdatedState(cuts)
    val cut = CutCalculator.cutFor(scenario.first, scenario.second)

    PlatformBackHandler(true) { voice.stop(); onBack() }
    KeepScreenOn(voice.on)

    fun newScenario() { scenario = CutCalculator.randomScenario(pool); n++; shown = false }

    fun startVoice() {
        if (shown) newScenario()
        voice.start {
            while (true) {
                val s = k
                val now = CutCalculator.cutFor(scenario.first, scenario.second)
                runner.run(SpokenSequence.cutFull(now, s.think, s.step), s.rate) { if (it == "reveal") shown = true }
                delay((k.next * 1000).toLong())
                newScenario()
            }
        }
    }

    fun act() {
        if (cuts.voice) { if (voice.on) voice.stop() else startVoice(); return }
        if (shown) newScenario() else shown = true
    }

    fun skip() {
        if (cuts.voice) {
            val was = voice.on
            voice.stop(); newScenario()
            if (was) startVoice()
        } else act()
    }

    val swipe = with(LocalDensity.current) { 50.dp.toPx() }
    Screen {
        Box(
            Modifier.fillMaxWidth().pointerInput(Unit) {
                var dx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dx = 0f },
                    onDragEnd = { if (dx < -swipe) skip() },
                    onHorizontalDrag = { _, d -> dx += d },
                )
            },
        ) {
            Column(Modifier.fillMaxWidth()) {
                TopBar(
                    "‹ Volver", { voice.stop(); onBack() }, center = { BarTitle("Cortes") },
                    right = { Text("Nº $n", color = Pal.Ink, fontWeight = FontWeight.Bold) },
                )
                Segmented(
                    listOf(false to "Manual", true to "Con voz"), cuts.voice,
                    { voice.stop(); onCuts(cuts.copy(voice = it)) }, Modifier.padding(top = 16.dp),
                )
                BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 26.dp), contentAlignment = Alignment.Center) {
                    val w = minOf(maxWidth * 0.36f, 150.dp)
                    Row(
                        Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            if (!cuts.voice && !shown) shown = true
                        },
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BigCard(cut.c, w, badgeVisible = shown)
                        Text("→", color = Pal.Muted, fontSize = 25.6.sp)
                        Column(
                            Modifier.size(w, w * 1.4f).drawBehind {
                                drawRoundRect(
                                    Color(0x59FFFFFF), cornerRadius = CornerRadius(size.width * 0.07f),
                                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                                )
                            },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                        ) {
                            Text("POSICIÓN", color = Pal.Muted, fontSize = 11.5.sp, letterSpacing = 0.15.em)
                            Text(cut.dest.toString(), color = Pal.Gold, fontFamily = Serif, fontSize = 54.sp)
                        }
                    }
                }
                if (!shown) {
                    Text("¿Qué carta cortas y adónde la llevas?", color = Pal.Muted, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp))
                } else {
                    CutSolution(cut)
                }
                Box(Modifier.padding(top = 18.dp)) {
                    ControlRow(
                        if (cuts.voice) (if (voice.on) "❚❚ Pausa" else "▶ Cantar") else (if (shown) "Siguiente ›" else "Revelar"),
                        ::act, if (cuts.voice) "›" else null, ::skip,
                    )
                }
                VoiceNotice(voice.error)
                if (cuts.voice) {
                    LabeledSlider("Pausa para pensar", cuts.think, "${dec(cuts.think)} s", 0.0, 15.0, 0.5) { onCuts(cuts.copy(think = it)) }
                    LabeledSlider("Pausa entre pasos de la explicación", cuts.step, "${decTrim(cuts.step)} s", 0.0, 4.0, 0.25) { onCuts(cuts.copy(step = it)) }
                    LabeledSlider("Pausa hasta el siguiente", cuts.next, "${dec(cuts.next)} s", 0.5, 8.0, 0.5) { onCuts(cuts.copy(next = it)) }
                    LabeledSlider("Velocidad de la voz", cuts.rate, "${dec(cuts.rate)}×", 0.6, 1.4, 0.1) { onCuts(cuts.copy(rate = it)) }
                }
            }
        }
    }
}

@Composable
private fun CutSolution(cut: com.corte53.domain.Cut) {
    val c = MnemonicaStack.cardAt(cut.c)
    val steps = buildList {
        add(buildAnnotatedString { append("El "); card(c); append(" está en la ${cut.c}: "); num("C = ${cut.c}"); append(".") })
        if (cut.up) {
            add(buildAnnotatedString { append("Destino "); num("D = ${cut.dest}"); append(". D > C: "); hint("tienes que bajar la carta"); append(".") })
            add(buildAnnotatedString { append("D − C = ${cut.dest} − ${cut.c} = "); num(cut.d); append(".") })
            add(buildAnnotatedString { append("N = 53 − (D − C) = 53 − ${cut.d} = "); num(cut.n); append(".") })
        } else {
            add(buildAnnotatedString { append("Destino "); num("D = ${cut.dest}"); append(". D < C: "); hint("tienes que subir la carta"); append(".") })
            add(buildAnnotatedString { append("N = C − D = ${cut.c} − ${cut.dest} = "); num(cut.n); append(".") })
        }
        add(buildAnnotatedString { append("En N = ${cut.n} está el "); card(cut.refCard); append(".") })
    }
    Column(Modifier.fillMaxWidth().padding(top = 18.dp).background(Pal.Panel, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            steps.forEach { Text(it, color = Pal.Ink, fontSize = 16.sp, lineHeight = 21.6.sp) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigCard(cut.n, 76.dp, badgeVisible = true, modifier = Modifier.padding(top = 5.dp, end = 5.dp))
            Column {
                Text(
                    if (cut.up) "↑ Cortar arriba" else "↓ Cortar abajo", color = if (cut.up) Pal.Green else Pal.Orange,
                    fontSize = 21.6.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.04.em,
                )
                val moved = cut.passedCards
                Text(
                    "El ${cut.refCard.longName} queda ${if (cut.up) "arriba" else "abajo"} del todo " +
                        "(pasas $moved ${if (moved == 1) "carta" else "cartas"} de arriba abajo).",
                    color = Pal.Muted, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

// =====================================================================================================
// Entrenar restas
// =====================================================================================================

@Composable
fun SubtractionsScreen(subs: SubtractionSettings, onSubs: (SubtractionSettings) -> Unit, speech: SpeechEngine, onBack: () -> Unit) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    var n by remember { mutableStateOf(1) }
    var sub by remember { mutableStateOf(SubtractionGenerator.next(subs.mode)) }
    var shown by remember { mutableStateOf(false) }
    val r by rememberUpdatedState(subs)

    PlatformBackHandler(true) { voice.stop(); onBack() }
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

    Screen {
        TopBar(
            "‹ Volver", { voice.stop(); onBack() }, center = { BarTitle("Restas") },
            right = { Text("Nº $n", color = Pal.Ink, fontWeight = FontWeight.Bold) },
        )
        Segmented(
            listOf(SubtractionMode.DIFF to "Entre dos números", SubtractionMode.COMPLEMENT to "Complemento a 53"), subs.mode,
            {
                val was = voice.on
                voice.stop(); onSubs(subs.copy(mode = it)); newSub(it)
                if (was) cycle()
            },
            Modifier.padding(top = 16.dp),
        )
        BoxWithConstraints(
            Modifier.fillMaxWidth().padding(top = 26.dp).background(Pal.Panel, RoundedCornerShape(18.dp))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { manual() }
                .padding(horizontal = 12.dp, vertical = 28.dp),
        ) {
            val size = (maxWidth.value * 0.13f).coerceIn(41.6f, 67.2f).sp
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sub.a.toString(), color = Pal.Ink, fontFamily = Serif, fontSize = size)
                    Text("−", color = Pal.Muted, fontFamily = Serif, fontSize = size, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(sub.b.toString(), color = Pal.Ink, fontFamily = Serif, fontSize = size)
                    Text("=", color = Pal.Muted, fontFamily = Serif, fontSize = size, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        if (shown) sub.result.toString() else "?", fontFamily = Serif, fontSize = size,
                        color = if (shown) Pal.Gold else Color(0x47FFFFFF), textAlign = TextAlign.Center, modifier = Modifier.width((size.value * 1.6f).dp),
                    )
                }
                Text(
                    if (voice.on) "" else if (shown) "Toca para la siguiente" else "Toca para ver el resultado",
                    color = Pal.Muted, fontSize = 14.4.sp, modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
        Box(Modifier.padding(top = 18.dp)) {
            ControlRow(if (voice.on) "❚❚ Pausa" else "▶ Cantar", ::play, "›") {
                val was = voice.on
                voice.stop(); newSub()
                if (was) cycle()
            }
        }
        VoiceNotice(voice.error)
        LabeledSlider("Pausa hasta el resultado", subs.think, "${dec(subs.think)} s", 0.0, 8.0, 0.5) { onSubs(subs.copy(think = it)) }
        LabeledSlider("Pausa hasta la siguiente", subs.gap, "${dec(subs.gap)} s", 0.5, 6.0, 0.5) { onSubs(subs.copy(gap = it)) }
        LabeledSlider("Velocidad de la voz", subs.rate, "${dec(subs.rate)}×", 0.6, 1.4, 0.1) { onSubs(subs.copy(rate = it)) }
    }
}
