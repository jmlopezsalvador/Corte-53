package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlin.math.round

/** Identidad visual: el rojo del icono de la app sobre vino oscuro, con crema de naipe como color de acción. */
object Pal {
    val Wine = Color(0xFF1C0509)        // fondo
    val Wine2 = Color(0xFF3A0912)       // centro del degradado
    val Surface = Color(0xFF2B0A11)     // paneles y hojas
    val SurfaceHi = Color(0xFF3D111B)
    val Line = Color(0x33F2E8D5)        // filetes y bordes
    val Red = Color(0xFF9F0311)         // rojo del icono: dorso, héroe
    val RedDeep = Color(0xFF6E020D)
    val RedHi = Color(0xFFC8102E)       // rojo naipe: palos, acentos
    val Cream = Color(0xFFF2E8D5)       // acción, selección, texto principal
    val CreamDim = Color(0xFFC7B8A2)    // texto secundario
    val Paper = Color(0xFFFFFAF0)       // cara de la carta
    val Black = Color(0xFF151515)
    val Badge = Color(0xFF222222)
    val Ok = Color(0xFF2E9E5B)
    val Ko = Color(0xFFC0392B)
    val Green = Color(0xFF7BE0A2)
    val Salmon = Color(0xFFFF9A8F)
    val Amber = Color(0xFFFFC46B)
    val Rose = Color(0xFFE9B7A8)        // anotaciones en cursiva
}

val Serif = FontFamily.Serif

fun Modifier.appBackground(): Modifier = this
    .background(Pal.Wine)
    .drawBehind {
        drawRect(
            Brush.radialGradient(
                0f to Pal.Wine2, 1f to Pal.Wine,
                center = Offset(size.width * 0.5f, size.height * 0.12f), radius = size.maxDimension * 0.85f,
            ),
        )
    }

/** Panel de superficie con esquinas de 18 dp. */
fun Modifier.panel(radius: Int = 18): Modifier = this
    .background(Pal.Surface, RoundedCornerShape(radius.dp))
    .border(1.dp, Pal.Line, RoundedCornerShape(radius.dp))

/** Decimal con coma española: [decimals] fijos (1 → «1,5», «5,0»). */
fun dec(x: Double, decimals: Int = 1): String {
    var p = 1.0
    repeat(decimals) { p *= 10 }
    val scaled = round(x * p).toLong()
    val sign = if (scaled < 0) "-" else ""
    val abs = kotlin.math.abs(scaled)
    val whole = abs / p.toLong()
    val frac = (abs % p.toLong()).toString().padStart(decimals, '0')
    return if (decimals == 0) "$sign$whole" else "$sign$whole,$frac"
}

/** Como [dec] pero sin ceros sobrantes (0,75 · 1 · 0,5). */
fun decTrim(x: Double, maxDecimals: Int = 2): String = dec(x, maxDecimals).trimEnd('0').trimEnd(',')
