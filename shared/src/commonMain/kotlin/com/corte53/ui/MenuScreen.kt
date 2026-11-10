package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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

@Composable
private fun NumberSelect(value: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Box(
            Modifier.fillMaxWidth().background(Color(0x40000000), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp))
                .clickable { open = true }.padding(12.dp),
            contentAlignment = Alignment.Center,
        ) { Text(value.toString(), color = Pal.Ink, fontSize = 16.sp) }
        DropdownMenu(open, { open = false }, modifier = Modifier.background(Pal.Felt)) {
            (1..52).forEach { n ->
                DropdownMenuItem(text = { Text(n.toString(), color = Pal.Ink) }, onClick = { onSelect(n); open = false })
            }
        }
    }
}

@Composable
fun MenuScreen(
    s: MenuSettings, onChange: (MenuSettings) -> Unit,
    onStart: () -> Unit, onListen: () -> Unit, onCuts: () -> Unit, onSubs: () -> Unit,
    onDeck: () -> Unit, onGloss: () -> Unit, voiceSection: @Composable () -> Unit,
) {
    val positions = PositionFilter.positions(s.from, s.to, s.filter)
    Screen {
        Text(
            "Mnemónica", color = Pal.Ink, fontFamily = Serif, fontSize = 33.6.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            "ENTRENADOR · TAMARIZ", color = Pal.Gold, fontSize = 12.5.sp, letterSpacing = 0.25.em,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Text(
            Suit.entries.sortedBy { listOf('P', 'H', 'C', 'D').indexOf(it.code) }.joinToString("") { it.symbol },
            color = Pal.Gold.copy(alpha = 0.8f), fontSize = 17.6.sp, letterSpacing = 0.5.em, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )

        Label("Orden")
        Segmented(listOf(false to "Secuencial", true to "Aleatorio"), s.random, { onChange(s.copy(random = it)) })
        Hint(if (s.random) "Todas las del rango, barajadas y sin repetir." else "Una detrás de otra, de la primera posición a la última.")

        Label("Qué te enseño")
        Segmented(
            listOf(TeachMode.NUMBER to "Número", TeachMode.CARD to "Carta", TeachMode.MIXED to "Mixto"),
            s.teach, { onChange(s.copy(teach = it)) },
        )
        Hint(
            when (s.teach) {
                TeachMode.NUMBER -> "Te muestro el número y tú dices la carta."
                TeachMode.CARD -> "Te muestro la carta y tú dices su número."
                TeachMode.MIXED -> "A veces número, a veces carta, al azar."
            },
        )

        Label("Posiciones")
        fun range(f: Int, t: Int) = onChange(s.copy(from = minOf(f, t), to = maxOf(f, t)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberSelect(s.from, { range(it, s.to) }, Modifier.weight(1f))
            Text("a", color = Pal.Muted)
            NumberSelect(s.to, { range(s.from, it) }, Modifier.weight(1f))
        }
        Chips {
            listOf("Todas" to (1 to 52), "1–13" to (1 to 13), "14–26" to (14 to 26), "27–39" to (27 to 39), "40–52" to (40 to 52))
                .forEach { (label, r) -> Chip(label, s.from == r.first && s.to == r.second) { range(r.first, r.second) } }
        }

        Label("Cartas")
        Segmented(
            listOf(FilterKind.ALL to "Todas", FilterKind.SUIT to "Un palo", FilterKind.RANK to "Un valor"),
            s.filterKind, { onChange(s.copy(filterKind = it)) },
        )
        if (s.filterKind == FilterKind.SUIT) Chips {
            listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS).forEach {
                Chip("${it.symbol} ${it.plural.replaceFirstChar { c -> c.uppercase() }}", s.suit == it, red = it.red) { onChange(s.copy(suit = it)) }
            }
        }
        if (s.filterKind == FilterKind.RANK) Chips {
            Rank.entries.forEach { r -> Chip(r.label, s.rank == r) { onChange(s.copy(rank = r)) } }
        }
        Hint(
            if (s.filterKind == FilterKind.ALL) "Todas las cartas del rango."
            else if (positions.isEmpty()) "Ninguna carta con ese filtro en el rango elegido."
            else "Posiciones: ${positions.joinToString(", ")}.",
        )

        val n = positions.size
        PrimaryButton(
            if (n == 0) "Sin cartas que entrenar" else "Empezar · $n ${if (n == 1) "carta" else "cartas"}",
            Modifier.padding(top = 28.dp),
        ) { if (n > 0) onStart() }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
            GhostButton("Escuchar en audio", Modifier.fillMaxWidth(), onClick = onListen)
            GhostButton("Entrenar cortes", Modifier.fillMaxWidth(), onClick = onCuts)
            GhostButton("Entrenar restas", Modifier.fillMaxWidth(), onClick = onSubs)
            GhostButton("Ver la baraja completa", Modifier.fillMaxWidth(), onClick = onDeck)
            GhostButton("Glosario de fórmulas", Modifier.fillMaxWidth(), onClick = onGloss)
        }
        voiceSection()
    }
}
