package com.corte53.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class Ic { Play, Pause, Back, Next, Close, Tune, Gear, Learn, Practice, Library, Reset, Speaker, Up, Down, Check }

/** Iconos dibujados con Canvas (no hay material-icons-extended en iOS). */
@Composable
fun Icon(ic: Ic, tint: Color = Pal.Cream, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val st = Stroke(width = s * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun o(x: Float, y: Float) = Offset(x * s, y * s)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(tint, o(x1, y1), o(x2, y2), st.width, StrokeCap.Round)
        fun poly(vararg pts: Pair<Float, Float>, close: Boolean = false, fill: Boolean = false) {
            val p = Path().apply {
                pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * s, y * s) else lineTo(x * s, y * s) }
                if (close) close()
            }
            drawPath(p, tint, style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else st)
        }
        when (ic) {
            Ic.Play -> poly(0.30f to 0.18f, 0.82f to 0.5f, 0.30f to 0.82f, close = true, fill = true)
            Ic.Pause -> {
                drawRoundRect(tint, o(0.25f, 0.18f), Size(0.18f * s, 0.64f * s), CornerRadius(0.05f * s))
                drawRoundRect(tint, o(0.57f, 0.18f), Size(0.18f * s, 0.64f * s), CornerRadius(0.05f * s))
            }
            Ic.Back -> poly(0.62f to 0.2f, 0.32f to 0.5f, 0.62f to 0.8f)
            Ic.Next -> poly(0.38f to 0.2f, 0.68f to 0.5f, 0.38f to 0.8f)
            Ic.Close -> { line(0.24f, 0.24f, 0.76f, 0.76f); line(0.76f, 0.24f, 0.24f, 0.76f) }
            Ic.Check -> poly(0.2f to 0.52f, 0.42f to 0.74f, 0.8f to 0.28f)
            Ic.Up -> { line(0.5f, 0.82f, 0.5f, 0.2f); poly(0.26f to 0.44f, 0.5f to 0.2f, 0.74f to 0.44f) }
            Ic.Down -> { line(0.5f, 0.18f, 0.5f, 0.8f); poly(0.26f to 0.56f, 0.5f to 0.8f, 0.74f to 0.56f) }
            Ic.Tune -> {
                listOf(0.25f to 0.35f, 0.5f to 0.68f, 0.75f to 0.45f).forEach { (y, x) ->
                    line(0.14f, y, 0.86f, y)
                    drawCircle(Pal.Wine, 0.1f * s, o(x, y))
                    drawCircle(tint, 0.1f * s, o(x, y), style = st)
                }
            }
            Ic.Gear -> {
                drawCircle(tint, 0.17f * s, o(0.5f, 0.5f), style = st)
                for (k in 0 until 8) {
                    val a = k * PI.toFloat() / 4
                    line(0.5f + 0.29f * cos(a), 0.5f + 0.29f * sin(a), 0.5f + 0.40f * cos(a), 0.5f + 0.40f * sin(a))
                }
                drawCircle(tint, 0.29f * s, o(0.5f, 0.5f), style = st)
            }
            Ic.Learn -> {
                rotate(-14f, o(0.5f, 0.5f)) {
                    drawRoundRect(tint, o(0.2f, 0.16f), Size(0.42f * s, 0.6f * s), CornerRadius(0.07f * s), style = st)
                }
                rotate(8f, o(0.5f, 0.5f)) {
                    drawRoundRect(tint, o(0.38f, 0.24f), Size(0.42f * s, 0.6f * s), CornerRadius(0.07f * s), style = st)
                }
            }
            Ic.Practice -> {
                drawRoundRect(tint, o(0.2f, 0.14f), Size(0.6f * s, 0.72f * s), CornerRadius(0.08f * s), style = st)
                line(0.08f, 0.5f, 0.92f, 0.5f)
            }
            Ic.Library -> {
                for (r in 0..1) for (c in 0..1) {
                    drawRoundRect(tint, o(0.16f + c * 0.36f, 0.16f + r * 0.36f), Size(0.28f * s, 0.28f * s), CornerRadius(0.06f * s), style = st)
                }
            }
            Ic.Reset -> {
                drawArc(tint, -50f, 290f, false, o(0.2f, 0.2f), Size(0.6f * s, 0.6f * s), style = st)
                poly(0.62f to 0.12f, 0.68f to 0.30f, 0.86f to 0.26f)
            }
            Ic.Speaker -> {
                poly(0.14f to 0.4f, 0.3f to 0.4f, 0.48f to 0.24f, 0.48f to 0.76f, 0.3f to 0.6f, 0.14f to 0.6f, close = true, fill = true)
                drawArc(tint, -45f, 90f, false, o(0.34f, 0.34f), Size(0.32f * s, 0.32f * s), style = st)
                drawArc(tint, -50f, 100f, false, o(0.22f, 0.22f), Size(0.56f * s, 0.56f * s), style = st)
            }
        }
    }
}
