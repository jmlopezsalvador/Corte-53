package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.domain.PositionFilter
import com.corte53.domain.Rank
import com.corte53.domain.Suit
import com.corte53.domain.TeachMode

/** Texto del filtro activo para la pantalla de audio («solo picas», «solo 8s»). */
fun filterLabel(m: MenuSettings): String = when (m.filterKind) {
    FilterKind.SUIT -> "solo ${m.suit.plural}"
    FilterKind.RANK -> "solo ${m.rank.label}s"
    FilterKind.ALL -> ""
}

private fun summary(m: MenuSettings): String {
    val order = if (m.random) "barajadas" else "en orden"
    val what = when (m.teach) {
        TeachMode.NUMBER -> "Te enseño el número y dices la carta."
        TeachMode.CARD -> "Te enseño la carta y dices el número."
        TeachMode.MIXED -> "A veces el número, a veces la carta."
    }
    val only = when (m.filterKind) {
        FilterKind.ALL -> ""
        FilterKind.SUIT -> " Solo ${m.suit.plural}."
        FilterKind.RANK -> " Solo los ${m.rank.label}."
    }
    return "Posiciones ${m.from} a ${m.to}, $order. $what$only"
}

private val QUICK = listOf("Todas" to (1 to 52), "1–13" to (1 to 13), "14–26" to (14 to 26), "27–39" to (27 to 39), "40–52" to (40 to 52))

/** Pestaña Memorizar: la selección actual y dos formas de repasarla. */
@Composable
fun ColumnScope.LearnTab(s: MenuSettings, onChange: (MenuSettings) -> Unit, onStart: () -> Unit, onListen: () -> Unit) {
    var sheet by remember { mutableStateOf(false) }
    val count = PositionFilter.positions(s.from, s.to, s.filter).size

    Column(Modifier.padding(top = 18.dp)) {
        Wordmark(32f)
        Text("El orden Mnemónica de Juan Tamariz", color = Pal.CreamDim, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
    }

    // Héroe: lo que vas a repasar, con el dorso de la carta y el número de cartas en su círculo
    Box(
        Modifier.padding(top = 22.dp).fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Pal.Red, Pal.RedDeep))),
    ) {
        CardBack(count, 118.dp, Modifier.align(Alignment.CenterEnd).offset(x = 12.dp, y = 30.dp).rotate(9f))
        Column(Modifier.padding(22.dp).fillMaxWidth(0.62f), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    if (count == 0) "Sin cartas" else "$count ${if (count == 1) "carta" else "cartas"}",
                    color = Pal.Cream, fontFamily = Serif, fontSize = 34.sp,
                )
                Text(
                    if (count == 0) "Ninguna carta con ese filtro en el rango elegido." else summary(s),
                    color = Pal.Cream.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    SectionTitle("Posiciones")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        QUICK.forEach { (label, r) ->
            Chip(label, s.from == r.first && s.to == r.second, Modifier.weight(1f), pad = 2.dp) { onChange(s.copy(from = r.first, to = r.second)) }
        }
    }
    SecondaryButton("Ajustar orden, rango y cartas", Modifier.padding(top = 14.dp), icon = Ic.Tune) { sheet = true }

    Column(Modifier.padding(top = 28.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton("Empezar con tarjetas", icon = Ic.Play, enabled = count > 0, onClick = onStart)
        SecondaryButton("Escuchar", icon = Ic.Speaker, enabled = count > 0, onClick = onListen)
    }

    if (sheet) FilterSheet(s, onChange) { sheet = false }
}

@Composable
private fun NumberSelect(value: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Box(
            Modifier.fillMaxWidth().height(52.dp).background(Pal.Wine, RoundedCornerShape(14.dp))
                .border(1.dp, Pal.Line, RoundedCornerShape(14.dp)).clickable { open = true },
            contentAlignment = Alignment.Center,
        ) { Text(value.toString(), color = Pal.Cream, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        DropdownMenu(open, { open = false }, modifier = Modifier.background(Pal.SurfaceHi)) {
            (1..52).forEach { n ->
                DropdownMenuItem(text = { Text(n.toString(), color = Pal.Cream, fontSize = 18.sp) }, onClick = { onSelect(n); open = false })
            }
        }
    }
}

@Composable
private fun FilterSheet(s: MenuSettings, onChange: (MenuSettings) -> Unit, onDismiss: () -> Unit) {
    Sheet("Qué repasar", onDismiss) {
        SectionTitle("Orden")
        Segmented(listOf(false to "En orden", true to "Barajadas"), s.random, { onChange(s.copy(random = it)) })

        SectionTitle("Qué te enseño")
        Segmented(
            listOf(TeachMode.NUMBER to "Número", TeachMode.CARD to "Carta", TeachMode.MIXED to "Mixto"),
            s.teach, { onChange(s.copy(teach = it)) },
        )
        Hint(
            when (s.teach) {
                TeachMode.NUMBER -> "Te muestro el número y tú dices la carta."
                TeachMode.CARD -> "Te muestro la carta y tú dices su número."
                TeachMode.MIXED -> "A veces el número, a veces la carta, al azar."
            },
        )

        SectionTitle("Posiciones")
        fun range(f: Int, t: Int) = onChange(s.copy(from = minOf(f, t), to = maxOf(f, t)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Desde", color = Pal.CreamDim, fontSize = 14.sp)
            NumberSelect(s.from, { range(it, s.to) }, Modifier.weight(1f))
            Text("hasta", color = Pal.CreamDim, fontSize = 14.sp)
            NumberSelect(s.to, { range(s.from, it) }, Modifier.weight(1f))
        }

        SectionTitle("Cartas")
        Segmented(
            listOf(FilterKind.ALL to "Todas", FilterKind.SUIT to "Un palo", FilterKind.RANK to "Un valor"),
            s.filterKind, { onChange(s.copy(filterKind = it)) },
        )
        if (s.filterKind == FilterKind.SUIT) Chips(Modifier.padding(top = 12.dp)) {
            listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS).forEach {
                Chip("${it.symbol} ${it.plural.replaceFirstChar { c -> c.uppercase() }}", s.suit == it) { onChange(s.copy(suit = it)) }
            }
        }
        if (s.filterKind == FilterKind.RANK) Chips(Modifier.padding(top = 12.dp)) {
            Rank.entries.forEach { r -> Chip(r.label, s.rank == r) { onChange(s.copy(rank = r)) } }
        }
        val positions = PositionFilter.positions(s.from, s.to, s.filter)
        Hint(
            if (s.filterKind == FilterKind.ALL) "Todas las cartas del rango."
            else if (positions.isEmpty()) "Ninguna carta con ese filtro en el rango elegido."
            else "Posiciones: ${positions.joinToString(", ")}.",
        )
        PrimaryButton("Listo", Modifier.padding(top = 24.dp), onClick = onDismiss)
    }
}
