package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.domain.Direction
import com.corte53.domain.MnemonicaStack
import kotlin.time.TimeSource

/** Estado de una sesión de entrenamiento: lista, qué se pregunta en cada carta y marca opcional (✓/✗/sin marcar). */
class TrainSession(val list: List<Int>, val dirs: List<Direction>) {
    val res = mutableStateListOf<Boolean?>().apply { repeat(list.size) { add(null) } }
    private val start = TimeSource.Monotonic.markNow()
    fun seconds(): Int = start.elapsedNow().inWholeSeconds.toInt()
}

private fun Int.clock(): String = "${this / 60}:${(this % 60).toString().padStart(2, '0')}"

@Composable
fun TrainScreen(session: TrainSession, onQuit: () -> Unit, onFinish: () -> Unit) {
    var i by remember { mutableStateOf(0) }
    val total = session.list.size
    val pos = session.list[i]
    val dir = session.dirs[i]
    val mark = session.res[i]
    val ok = session.res.count { it == true }
    val ko = session.res.count { it == false }

    // estado visual de la carta actual: se reinicia (sin animar) al cambiar de carta
    var flipped by remember(i) { mutableStateOf(false) }
    var revealed by remember(i) { mutableStateOf(false) }

    fun go(step: Int) {
        val n = i + step
        if (n < 0) return
        if (n >= total) onFinish() else i = n
    }
    fun toggle() {
        flipped = !flipped
        if (flipped) revealed = true
    }
    fun answer(good: Boolean) {
        if (!revealed) return
        session.res[i] = good
        go(1)
    }
    PlatformBackHandler(true, onQuit)

    Screen(scroll = false) {
        TopBar(
            "✕ Salir", onQuit,
            center = { Text("${i + 1} / $total", color = Pal.Ink, fontWeight = FontWeight.Bold) },
            right = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("✓ $ok", color = Pal.Green, fontWeight = FontWeight.Bold)
                    Text("✗ $ko", color = Pal.Salmon, fontWeight = FontWeight.Bold)
                }
            },
        )
        Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(4.dp).background(Color(0x4D000000), RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(i.toFloat() / total).fillMaxHeight().background(Pal.Gold))
        }

        val swipe = with(LocalDensity.current) { 50.dp.toPx() }
        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().pointerInput(i) {
                var dx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dx = 0f },
                    onDragEnd = { if (dx < -swipe) go(1) else if (dx > swipe) go(-1) },
                    onHorizontalDrag = { _, d -> dx += d },
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            val w: Dp = minOf(maxWidth * 0.64f, 250.dp, (maxHeight - 40.dp) / 1.4f)
            Box(Modifier.clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { toggle() }) {
                if (dir == Direction.NUMBER_TO_CARD) {
                    FlipCard(flipped, w, front = { CardBack(pos, w) }, back = { BigCard(pos, w, badgeVisible = true) })
                } else {
                    // Carta → número: la carta no gira; solo aparece el círculo con la posición
                    BigCard(pos, w, badgeVisible = flipped)
                }
            }
        }

        val info = when (mark) { true -> " · marcada ✓"; false -> " · marcada ✗"; null -> "" }
        Tip(
            (if (revealed) "¿Lo sabías?" else if (dir == Direction.NUMBER_TO_CARD) "¿Qué carta está en esta posición?" else "¿En qué posición está esta carta?") + info,
        )
        Row(Modifier.fillMaxWidth().alpha(if (revealed) 1f else 0f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            @Composable
            fun Mark(text: String, color: Color, chosen: Boolean, mod: Modifier, onClick: () -> Unit) = Box(
                mod.background(color, RoundedCornerShape(14.dp))
                    .then(if (chosen) Modifier.border(3.dp, Pal.Ink, RoundedCornerShape(14.dp)) else Modifier)
                    .clickable(enabled = revealed, onClick = onClick).padding(17.dp),
                contentAlignment = Alignment.Center,
            ) { Text(text, color = Color.White, fontSize = 16.8.sp, fontWeight = FontWeight.Bold) }
            Mark("✗ Fallé", Pal.Ko, mark == false, Modifier.weight(1f)) { answer(false) }
            Mark("✓ Acerté", Pal.Ok, mark == true, Modifier.weight(1f)) { answer(true) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton("‹ Anterior", Modifier.weight(1f), enabled = i > 0, fontSize = 16f) { go(-1) }
            GhostButton(if (i == total - 1) "Terminar ›" else "Siguiente ›", Modifier.weight(1f), fontSize = 16f) { go(1) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(session: TrainSession, onRetryFails: () -> Unit, onAgain: () -> Unit, onMenu: () -> Unit) {
    val total = session.list.size
    val ok = session.res.count { it == true }
    val ko = session.res.count { it == false }
    val marked = ok + ko
    val skipped = total - marked
    val clock = session.seconds().clock()
    val fails = session.list.filterIndexed { k, _ -> session.res[k] == false }.sorted()
    PlatformBackHandler(true, onMenu)

    Screen {
        Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SESIÓN TERMINADA", color = Pal.Muted, fontSize = 12.sp, letterSpacing = 0.15.sp)
            Text(
                if (marked > 0) "$ok / $marked" else "$total ${if (total == 1) "carta" else "cartas"}",
                color = Pal.Ink, fontFamily = Serif, fontSize = 54.sp, modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                if (marked > 0) "${(ok * 100f / marked).let { kotlin.math.round(it).toInt() }}% de aciertos · $clock min" +
                    (if (skipped > 0) " · $skipped sin marcar" else "")
                else "Recorrido sin marcar · $clock min",
                color = Pal.Muted, modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center,
            )
        }
        if (fails.isNotEmpty()) {
            Label("Para repasar")
            FlowRow(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                fails.forEach { p ->
                    val c = MnemonicaStack.cardAt(p)
                    Row(
                        Modifier.background(Pal.Paper, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(p.toString(), color = Pal.Felt, fontWeight = FontWeight.Bold, fontSize = 15.2.sp)
                        Text(c.shortName, color = if (c.suit.red) Pal.Red else Pal.Black, fontWeight = FontWeight.Bold, fontSize = 15.2.sp)
                    }
                }
            }
        } else if (marked > 0) {
            Label("Para repasar")
            Text("¡Sin fallos! 🎩", color = Pal.Muted, fontSize = 17.6.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        if (fails.isNotEmpty()) PrimaryButton("Repasar fallos", Modifier.padding(top = 28.dp), onClick = onRetryFails)
        GhostButton("Repetir sesión", Modifier.fillMaxWidth().padding(top = 10.dp), onClick = onAgain)
        GhostButton("Menú", Modifier.fillMaxWidth().padding(top = 10.dp), onClick = onMenu)
    }
}
