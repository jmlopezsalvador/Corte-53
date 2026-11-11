package com.corte53.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SequenceRunner
import com.corte53.audio.SpeechEngine
import com.corte53.domain.Cut
import com.corte53.domain.CutCalculator
import com.corte53.domain.MnemonicaStack
import com.corte53.domain.PositionFilter
import com.corte53.domain.SpokenSequence
import kotlinx.coroutines.delay

@Composable
fun CutsScreen(
    menu: MenuSettings, cuts: CutSettings, onCuts: (CutSettings) -> Unit,
    speech: SpeechEngine, voiceId: String?, onBack: () -> Unit,
) {
    val runner = remember(speech) { SequenceRunner(speech) }
    val voice = rememberVoice()
    val pool = remember(menu) { PositionFilter.positions(menu.from, menu.to, menu.filter) }
    var n by remember { mutableStateOf(1) }
    var scenario by remember { mutableStateOf(CutCalculator.randomScenario(pool)) }
    var shown by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    val k by rememberUpdatedState(cuts)
    val cut = CutCalculator.cutFor(scenario.first, scenario.second)

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
    WorkScreen(
        "Cortes", { voice.stop(); onBack() },
        trailing = {
            BarCounter("Nº $n")
            if (cuts.voice) IconBtn(Ic.Tune, "Tiempos de la voz") { sheet = true }
        },
        controls = {
            PrimaryButton(
                if (cuts.voice) (if (voice.on) "Pausa" else "Cantar") else (if (shown) "Siguiente" else "Revelar"),
                Modifier.weight(1f), icon = if (cuts.voice) (if (voice.on) Ic.Pause else Ic.Play) else null, onClick = ::act,
            )
            if (cuts.voice) IconSquare(Ic.Next, "Otro corte", onClick = ::skip)
        },
    ) {
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
                Segmented(
                    listOf(false to "Manual", true to "Con voz"), cuts.voice,
                    { voice.stop(); onCuts(cuts.copy(voice = it)) }, Modifier.padding(top = 4.dp),
                )
                BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 28.dp), contentAlignment = Alignment.Center) {
                    val w = minOf(maxWidth * 0.36f, 150.dp)
                    Row(
                        Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            if (!cuts.voice && !shown) shown = true
                        },
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BigCard(cut.c, w, badgeVisible = shown, modifier = Modifier.padding(top = 8.dp, end = 8.dp))
                        Icon(Ic.Next, Pal.CreamDim, 26.dp)
                        Column(
                            Modifier.size(w, w * 1.4f).drawBehind {
                                drawRoundRect(
                                    Pal.Line, cornerRadius = CornerRadius(size.width * 0.07f),
                                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                                )
                            },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        ) {
                            Text("a la posición", color = Pal.CreamDim, fontSize = 13.sp)
                            Text(cut.dest.toString(), color = Pal.Cream, fontFamily = Serif, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (!shown) {
                    Text(
                        "¿Qué carta cortas para llevarla ahí?", color = Pal.CreamDim, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                    )
                } else CutSolution(cut)
                VoiceWarning(speech, voiceId)
                voice.error?.let { Warning(it) }
            }
        }
    }

    if (sheet) Sheet("Tiempos de la voz", { sheet = false }) {
        LabeledSlider("Pausa para pensar", cuts.think, "${dec(cuts.think)} s", 0.0, 15.0, 0.5) { onCuts(cuts.copy(think = it)) }
        LabeledSlider("Pausa entre pasos", cuts.step, "${decTrim(cuts.step)} s", 0.0, 4.0, 0.25) { onCuts(cuts.copy(step = it)) }
        LabeledSlider("Pausa hasta el siguiente", cuts.next, "${dec(cuts.next)} s", 0.5, 8.0, 0.5) { onCuts(cuts.copy(next = it)) }
        LabeledSlider("Velocidad de la voz", cuts.rate, "${dec(cuts.rate)}×", 0.6, 1.4, 0.1) { onCuts(cuts.copy(rate = it)) }
    }
}

@Composable
private fun CutSolution(cut: Cut) {
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
    Column(Modifier.fillMaxWidth().padding(top = 22.dp).panel().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEach { Text(it, color = Pal.CreamDim, fontSize = 16.sp, lineHeight = 22.sp) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigCard(cut.n, 76.dp, badgeVisible = true, modifier = Modifier.padding(top = 5.dp, end = 5.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(if (cut.up) Ic.Up else Ic.Down, if (cut.up) Pal.Green else Pal.Amber, 26.dp)
                    Text(
                        if (cut.up) "Cortar arriba" else "Cortar abajo", color = if (cut.up) Pal.Green else Pal.Amber,
                        fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                    )
                }
                val moved = cut.passedCards
                Text(
                    "El ${cut.refCard.longName} queda ${if (cut.up) "arriba" else "abajo"} del todo " +
                        "(pasas $moved ${if (moved == 1) "carta" else "cartas"} de arriba abajo).",
                    color = Pal.CreamDim, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
