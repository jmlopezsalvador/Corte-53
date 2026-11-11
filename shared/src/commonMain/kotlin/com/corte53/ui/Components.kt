package com.corte53.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

/** Pantalla: fondo vino, zonas seguras y columna de 520 dp máx. [bottomBar] ocupa todo el ancho, fija abajo. */
@Composable
fun Screen(scroll: Boolean = true, bottomBar: @Composable () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().appBackground().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        val base = Modifier.weight(1f).widthIn(max = 520.dp).fillMaxWidth().padding(horizontal = 20.dp)
        Column(if (scroll) base.verticalScroll(rememberScrollState()) else base, content = content)
        bottomBar()
    }
}

/** Marca: «Corte» y el 53 dentro del círculo de posición, como en las cartas. */
@Composable
fun Wordmark(size: Float = 30f) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((size * 0.28f).dp)) {
        Text("Corte", color = Pal.Cream, fontFamily = Serif, fontSize = size.sp)
        Box(Modifier.size((size * 1.25f).dp).background(Pal.RedHi, CircleShape), contentAlignment = Alignment.Center) {
            Text("53", color = Pal.Cream, fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = (size * 0.66f).sp)
        }
    }
}

/** Título de sección: serif, frase normal. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Pal.Cream, fontFamily = Serif, fontSize = 20.sp, modifier = modifier.padding(top = 28.dp, bottom = 10.dp))
}

@Composable
fun Hint(text: String, modifier: Modifier = Modifier, center: Boolean = false) {
    Text(
        text, color = Pal.CreamDim, fontSize = 14.sp, lineHeight = 20.sp,
        textAlign = if (center) TextAlign.Center else null,
        modifier = modifier.padding(top = 8.dp).then(if (center) Modifier.fillMaxWidth() else Modifier),
    )
}

@Composable
fun Warning(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Pal.Amber, fontSize = 14.sp, lineHeight = 20.sp, modifier = modifier.padding(top = 8.dp))
}

@Composable
fun <T> Segmented(
    options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit,
    modifier: Modifier = Modifier, perRow: Int = options.size,
) {
    Column(
        modifier.fillMaxWidth().background(Pal.Surface, RoundedCornerShape(14.dp)).border(1.dp, Pal.Line, RoundedCornerShape(14.dp)).padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.chunked(perRow).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { (value, text) ->
                    val sel = value == selected
                    Box(
                        Modifier.weight(1f).height(44.dp)
                            .background(if (sel) Pal.Cream else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onSelect(value) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text, color = if (sel) Pal.Red else Pal.Cream, fontSize = 15.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, pad: Dp = 16.dp, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier.height(40.dp)
            .background(if (selected) Pal.Cream else Color.Transparent, shape)
            .border(1.dp, if (selected) Pal.Cream else Pal.Line, shape)
            .clickable(onClick = onClick).padding(horizontal = pad),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 14.sp, maxLines = 1, softWrap = false, color = if (selected) Pal.Red else Pal.Cream, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Chips(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

/** Fila de chips con scroll horizontal. */
@Composable
fun ChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp), content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String, modifier: Modifier = Modifier, icon: Ic? = null, enabled: Boolean = true, onClick: () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().height(56.dp).alpha(if (enabled) 1f else 0.45f)
            .background(Pal.Cream, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) { Icon(icon, Pal.Red, 22.dp); Box(Modifier.size(10.dp)) }
        Text(text, color = Pal.Red, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(
    text: String, modifier: Modifier = Modifier, icon: Ic? = null, enabled: Boolean = true, onClick: () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().height(56.dp).alpha(if (enabled) 1f else 0.4f)
            .border(BorderStroke(1.dp, Pal.Line), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) { Icon(icon, Pal.Cream, 22.dp); Box(Modifier.size(10.dp)) }
        Text(text, color = Pal.Cream, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Botón cuadrado con icono (56 dp), para acciones secundarias junto al principal. */
@Composable
fun IconSquare(ic: Ic, description: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier.size(56.dp).alpha(if (enabled) 1f else 0.4f)
            .border(BorderStroke(1.dp, Pal.Line), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Icon(ic, Pal.Cream, 24.dp) }
}

/** Botón de icono de 44 dp para barras superiores. */
@Composable
fun IconBtn(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clickable(onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Icon(ic, Pal.Cream, 24.dp) }
}

/** Barra superior: atrás · título · acciones. */
@Composable
fun TopBar(title: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBtn(Ic.Back, "Volver", onBack)
        Text(title, color = Pal.Cream, fontFamily = Serif, fontSize = 22.sp, modifier = Modifier.weight(1f).padding(start = 4.dp))
        trailing()
    }
}

enum class Tab(val label: String, val icon: Ic) {
    Learn("Memorizar", Ic.Learn), Practice("Practicar", Ic.Practice), Library("Baraja", Ic.Library), Settings("Ajustes", Ic.Gear),
}

@Composable
fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Pal.Surface).drawBehind { drawLine(Pal.Line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { t ->
            val sel = t == selected
            Column(
                Modifier.weight(1f).clickable { onSelect(t) }.padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(width = 56.dp, height = 30.dp).background(if (sel) Pal.Red else Color.Transparent, RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(t.icon, if (sel) Pal.Cream else Pal.CreamDim, 22.dp) }
                Text(
                    t.label, fontSize = 12.sp, color = if (sel) Pal.Cream else Pal.CreamDim,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

/** Hoja inferior para ajustes de una pantalla. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Pal.Surface, contentColor = Pal.Cream,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Pal.Line) },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
        ) {
            Text(title, color = Pal.Cream, fontFamily = Serif, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
            content()
        }
    }
}

@Composable
fun LabeledSlider(
    label: String, value: Double, valueText: String, min: Double, max: Double, step: Double,
    onChange: (Double) -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Pal.Cream, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(valueText, color = Pal.Cream, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    val steps = (((max - min) / step).toInt() - 1).coerceAtLeast(0)
    Slider(
        value = value.toFloat(), onValueChange = { v -> onChange(round(v / step) * step) },
        valueRange = min.toFloat()..max.toFloat(), steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = Pal.Cream, activeTrackColor = Pal.RedHi, inactiveTrackColor = Pal.Line,
            activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
        ),
    )
}

@Composable
fun Toggle(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp).clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = Pal.Cream, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(
            checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Pal.Cream, checkedTrackColor = Pal.RedHi, checkedBorderColor = Pal.RedHi,
                uncheckedThumbColor = Pal.CreamDim, uncheckedTrackColor = Pal.Surface, uncheckedBorderColor = Pal.Line,
            ),
        )
    }
}
