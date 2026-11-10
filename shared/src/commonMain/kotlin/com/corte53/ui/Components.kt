package com.corte53.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.round

/** Colores de la identidad visual (04-UI-Y-DISENO.md). */
object Pal {
    val Felt = Color(0xFF0F3D2E)
    val Felt2 = Color(0xFF17604A)
    val Ink = Color(0xFFF3EFE3)
    val Muted = Color(0xFFB7C8BE)
    val Gold = Color(0xFFE0B84F)
    val Red = Color(0xFFC8102E)
    val Black = Color(0xFF151515)
    val Paper = Color(0xFFFFFDF7)
    val Ok = Color(0xFF2E9E5B)
    val Ko = Color(0xFFC0392B)
    val Green = Color(0xFF7BE0A2)
    val Salmon = Color(0xFFFF9A8F)
    val Orange = Color(0xFFFFC46B)
    val Teal = Color(0xFF9FD8C0)
    val Back = Color(0xFF8B1A2B)
    val Badge = Color(0xFF222222)
    val Panel = Color(0x3D000000)       // negro translúcido (~.24)
    val Line = Color(0x4DFFFFFF)        // blanco 30 %
}

val Serif = FontFamily.Serif

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

/** Fondo de tapete con degradado radial y columna centrada de 520 dp máx. */
@Composable
fun Screen(scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize()
            .background(Pal.Felt)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0f to Pal.Felt2, 0.7f to Pal.Felt,
                        center = Offset(size.width / 2, size.height * 0.25f),
                        radius = size.maxDimension,
                    ),
                )
            }
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val base = Modifier.widthIn(max = 520.dp).fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp)
        Column(if (scroll) base.verticalScroll(rememberScrollState()) else base, content = content)
    }
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), color = Pal.Muted, fontSize = 12.sp, letterSpacing = 0.15.em,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

@Composable
fun Hint(text: String, modifier: Modifier = Modifier, center: Boolean = false) {
    Text(
        text, color = Pal.Muted, fontSize = 13.6.sp, lineHeight = 19.sp,
        textAlign = if (center) TextAlign.Center else null,
        modifier = modifier.padding(top = 8.dp).then(if (center) Modifier.fillMaxWidth() else Modifier),
    )
}

@Composable
fun <T> Segmented(
    options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit,
    modifier: Modifier = Modifier, perRow: Int = options.size,
) {
    Column(
        modifier.fillMaxWidth().background(Color(0x40000000), RoundedCornerShape(12.dp)).padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.chunked(perRow).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { (value, text) ->
                    val sel = value == selected
                    Box(
                        Modifier.weight(1f)
                            .background(if (sel) Pal.Paper else Color.Transparent, RoundedCornerShape(9.dp))
                            .clickable { onSelect(value) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text, color = if (sel) Pal.Felt else Pal.Ink, fontSize = 14.7.sp,
                            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, red: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .background(if (selected) Pal.Gold else Color.Transparent, shape)
            .border(1.dp, if (selected) Pal.Gold else Color(0x40FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .widthIn(min = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text, fontSize = 13.6.sp,
            color = if (selected) Pal.Felt else if (red) Pal.Salmon else Pal.Ink,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Chips(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.6f)
            .background(Pal.Gold, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = Pal.Felt, fontSize = 16.8.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun GhostButton(
    text: String, modifier: Modifier = Modifier, enabled: Boolean = true, fontSize: Float = 16.8f,
    onClick: () -> Unit,
) {
    Box(
        modifier.alpha(if (enabled) 1f else 0.35f)
            .border(BorderStroke(1.dp, Pal.Line), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = Pal.Ink, fontSize = fontSize.sp, fontWeight = FontWeight.Bold) }
}

/** Barra superior de tres zonas: volver/salir · título/progreso · contador. */
@Composable
fun TopBar(left: String, onLeft: () -> Unit, center: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            Text(left, color = Pal.Muted, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onLeft).padding(vertical = 6.dp))
        }
        center()
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { right() }
    }
}

@Composable
fun BarTitle(text: String) = Text(text, color = Pal.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)

@Composable
fun LabeledSlider(
    label: String, value: Double, valueText: String, min: Double, max: Double, step: Double,
    onChange: (Double) -> Unit,
) {
    Text(
        buildAnnotatedString { append(label.uppercase()); append(" · "); append(valueText) },
        color = Pal.Muted, fontSize = 12.sp, letterSpacing = 0.15.em, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
    val steps = (((max - min) / step).toInt() - 1).coerceAtLeast(0)
    Slider(
        value = value.toFloat(), onValueChange = { v ->
            onChange(round(v / step) * step)
        },
        valueRange = min.toFloat()..max.toFloat(), steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = Pal.Gold, activeTrackColor = Pal.Gold, inactiveTrackColor = Color(0x33FFFFFF),
            activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
        ),
    )
}

@Composable
fun Toggle(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp).clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked, onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Pal.Gold, checkmarkColor = Pal.Felt, uncheckedColor = Pal.Muted,
            ),
        )
        Text(text, color = Pal.Ink, fontSize = 15.2.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

/** Círculo/aviso de texto de apoyo bajo una zona; ocupa su sitio aunque esté vacío. */
@Composable
fun Tip(text: String, modifier: Modifier = Modifier) {
    Text(
        text, color = Pal.Muted, fontSize = 16.sp, textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(bottom = 14.dp),
        style = TextStyle(lineHeight = 22.sp),
    )
}
