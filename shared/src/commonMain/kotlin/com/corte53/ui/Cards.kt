package com.corte53.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.corte53.domain.Card
import com.corte53.domain.MnemonicaStack
import com.corte53.domain.Rank

private val PIPS: Map<Rank, List<Pair<Int, Int>>> = mapOf(
    Rank.TWO to listOf(50 to 20, 50 to 80),
    Rank.THREE to listOf(50 to 20, 50 to 50, 50 to 80),
    Rank.FOUR to listOf(30 to 20, 70 to 20, 30 to 80, 70 to 80),
    Rank.FIVE to listOf(30 to 20, 70 to 20, 50 to 50, 30 to 80, 70 to 80),
    Rank.SIX to listOf(30 to 20, 70 to 20, 30 to 50, 70 to 50, 30 to 80, 70 to 80),
    Rank.SEVEN to listOf(30 to 20, 70 to 20, 50 to 35, 30 to 50, 70 to 50, 30 to 80, 70 to 80),
    Rank.EIGHT to listOf(30 to 20, 70 to 20, 50 to 35, 30 to 50, 70 to 50, 50 to 65, 30 to 80, 70 to 80),
    Rank.NINE to listOf(30 to 20, 70 to 20, 30 to 40, 70 to 40, 50 to 50, 30 to 60, 70 to 60, 30 to 80, 70 to 80),
    Rank.TEN to listOf(30 to 20, 70 to 20, 50 to 30, 30 to 40, 70 to 40, 30 to 60, 70 to 60, 50 to 70, 30 to 80, 70 to 80),
)

@Composable
private fun Dp.toSp(): TextUnit = with(LocalDensity.current) { this@toSp.toSp() }

/** Texto sin relleno de fuente, centrado en su línea, para que los símbolos queden donde se espera. */
@Composable
private fun Glyph(text: String, size: Dp, color: Color, bold: Boolean = false, serif: Boolean = true, spacing: TextUnit = TextUnit.Unspecified) {
    val sp = size.toSp()
    Text(
        text, color = color, fontSize = sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontFamily = if (serif) Serif else null, letterSpacing = spacing, textAlign = TextAlign.Center,
        softWrap = false, modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally, unbounded = true),
        style = TextStyle(
            lineHeight = sp,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        ),
    )
}

/** Cara de la carta, vectorial, escalada con el ancho [w] (proporción 1 : 1,4). */
@Composable
fun PlayingCard(card: Card, w: Dp, modifier: Modifier = Modifier) {
    val em = w / 10
    val h = w * 1.4f
    val color = if (card.suit.red) Pal.Red else Pal.Black
    val sym = card.suit.symbol
    val shape = RoundedCornerShape(w * 0.07f)
    Box(modifier.size(w, h).shadow(6.dp, shape).clip(shape).background(Pal.Paper)) {
        // índices (rango sobre palo): arriba a la izquierda y, girado, abajo a la derecha
        @Composable
        fun index(mod: Modifier) = Column(mod.width(em * 1.3f), horizontalAlignment = Alignment.CenterHorizontally) {
            Glyph(card.rank.label, em * 1.45f, color, bold = true, spacing = (-0.08).em)
            Box(Modifier.padding(top = em * 0.05f)) { Glyph(sym, em * 1.1f, color) }
        }
        index(Modifier.align(Alignment.TopStart).padding(top = em * 0.45f, start = em * 0.35f))
        index(Modifier.align(Alignment.BottomEnd).padding(bottom = em * 0.45f, end = em * 0.35f).rotate(180f))

        when (card.rank) {
            Rank.ACE -> Box(Modifier.align(Alignment.Center)) { Glyph(sym, em * 5.5f, color) }
            Rank.JACK, Rank.QUEEN, Rank.KING -> Column(
                Modifier.align(Alignment.Center)
                    .size(w * 0.62f, h * 0.68f)
                    .border(em * 0.08f, color, RoundedCornerShape(em * 0.4f))
                    .background(
                        Brush.linearGradient(listOf(Pal.Gold.copy(alpha = 0.25f), Pal.Gold.copy(alpha = 0.06f))),
                        RoundedCornerShape(em * 0.4f),
                    ),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Glyph(card.rank.label, em * 4.6f, color)
                Glyph(sym, em * 2.4f, color)
            }
            else -> PIPS.getValue(card.rank).forEach { (px, py) ->
                val s = em * 2.1f
                Box(
                    Modifier.offset(w * px / 100 - s / 2, h * py / 100 - s / 2).size(s)
                        .then(if (py > 50) Modifier.rotate(180f) else Modifier),
                    contentAlignment = Alignment.Center,
                ) { Glyph(sym, s, color) }
            }
        }
    }
}

/** Dorso granate con filete dorado y la posición en un círculo central. */
@Composable
fun CardBack(position: Int, w: Dp, modifier: Modifier = Modifier) {
    val em = w / 10
    val shape = RoundedCornerShape(w * 0.07f)
    Box(modifier.size(w, w * 1.4f).shadow(6.dp, shape).clip(shape).background(Pal.Paper)) {
        val inner = RoundedCornerShape(em * 0.5f)
        Canvas(
            Modifier.padding(em * 0.45f).size(w - em * 0.9f, w * 1.4f - em * 0.9f).clip(inner).background(Pal.Back),
        ) {
            val stripe = Color.White.copy(alpha = 0.13f)
            val step = size.width * 0.048f
            val sw = size.width * 0.008f
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(stripe, Offset(x, 0f), Offset(x + size.height, size.height), sw)
                drawLine(stripe, Offset(x + size.height, 0f), Offset(x, size.height), sw)
                x += step
            }
        }
        Box(
            Modifier.padding(em * 0.45f).size(w - em * 0.9f, w * 1.4f - em * 0.9f)
                .border(em * 0.12f, Pal.Gold, inner),
        )
        Box(
            Modifier.align(Alignment.Center).size(em * 6.08f)
                .background(Pal.Paper, CircleShape).border(em * 0.07f, Pal.Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Glyph(position.toString(), em * 3.2f, Pal.Felt, bold = true) }
    }
}

/** Círculo negro con la posición, en la esquina superior derecha. Aparece con fundido y rebote. */
@Composable
fun PositionBadge(position: Int, w: Dp, visible: Boolean = true, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(250))
    val scale by animateFloatAsState(
        if (visible) 1f else 0.5f, tween(300, easing = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)),
    )
    Box(
        modifier.size(w * 0.28f).graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
            .shadow(3.dp, CircleShape).background(Pal.Badge, CircleShape).border(w * 0.015f, Pal.Felt, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Glyph(position.toString(), w * 0.14f, Color.White, bold = true, serif = false) }
}

/** Carta grande con el círculo de posición ([badgeVisible] = false lo oculta para revelarlo después). */
@Composable
fun BigCard(position: Int, w: Dp, badgeVisible: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.size(w, w * 1.4f)) {
        PlayingCard(MnemonicaStack.cardAt(position), w)
        PositionBadge(position, w, badgeVisible, Modifier.align(Alignment.TopEnd).offset(w * 0.07f, -(w * 0.07f)))
    }
}

/** Gira en 3D: [flipped] false = [front], true = [back]. Sin animación si el estado cambia de clave. */
@Composable
fun FlipCard(flipped: Boolean, w: Dp, front: @Composable () -> Unit, back: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val angle by animateFloatAsState(
        if (flipped) 180f else 0f, tween(450, easing = CubicBezierEasing(0.3f, 0.7f, 0.3f, 1f)),
    )
    val density = LocalDensity.current.density
    Box(
        modifier.size(w, w * 1.4f).graphicsLayer { rotationY = angle; cameraDistance = 12f * density },
    ) {
        if (angle <= 90f) front() else Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
    }
}
