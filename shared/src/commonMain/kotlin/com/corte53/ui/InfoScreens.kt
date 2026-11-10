package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.domain.Card
import com.corte53.domain.CutCalculator
import com.corte53.domain.MnemonicaStack

// ---- Texto enriquecido compartido con la pantalla de Cortes ----

/** «7♦» en negro o en rojo salmón (fondo oscuro). */
fun AnnotatedString.Builder.card(c: Card) {
    withStyle(SpanStyle(color = if (c.suit.red) Pal.Salmon else Color.Unspecified)) { append(c.shortName) }
}

fun AnnotatedString.Builder.num(x: Any) =
    withStyle(SpanStyle(color = Pal.Gold, fontWeight = FontWeight.Bold)) { append(x.toString()) }

fun AnnotatedString.Builder.hint(text: String) =
    withStyle(SpanStyle(color = Pal.Teal, fontStyle = FontStyle.Italic)) { append(text) }

fun AnnotatedString.Builder.strong(text: String) =
    withStyle(SpanStyle(color = Pal.Ink, fontWeight = FontWeight.Bold)) { append(text) }

// ---- Baraja completa ----

@Composable
fun DeckScreen(onBack: () -> Unit) {
    PlatformBackHandler(true, onBack)
    Screen(scroll = false) {
        TopBar("‹ Volver", onBack, center = { BarTitle("La baraja") }, right = {})
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val w = (maxWidth - 62.dp) / 4
            LazyVerticalGrid(
                GridCells.Fixed(4), Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items((1..52).toList()) { p ->
                    Box(Modifier.padding(top = 7.dp, end = 7.dp)) {
                        PlayingCard(MnemonicaStack.cardAt(p), w)
                        Box(
                            Modifier.align(Alignment.TopEnd).offset(7.dp, (-7).dp).widthIn(min = 26.dp)
                                .background(Pal.Badge, CircleShape).border(2.dp, Pal.Felt, CircleShape).padding(horizontal = 4.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(p.toString(), color = Color.White, fontSize = 12.8.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ---- Glosario de fórmulas ----

@Composable
private fun GlossaryCase(
    title: String, tag: String, tagColor: Color, move: String, formula: String, body: AnnotatedString, example: AnnotatedString,
) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).background(Pal.Panel, RoundedCornerShape(14.dp)).padding(16.dp)) {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Pal.Ink, fontWeight = FontWeight.Bold, fontSize = 16.3.sp)
            Text(
                tag, color = tagColor, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.background(Color(0x1AFFFFFF), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Text(move, color = Pal.Teal, fontStyle = FontStyle.Italic, fontSize = 14.4.sp, modifier = Modifier.padding(top = 6.dp))
        Text(formula, color = Pal.Gold, fontFamily = Serif, fontSize = 21.6.sp, modifier = Modifier.padding(top = 12.dp))
        Text(body, color = Pal.Ink, fontSize = 15.5.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
        Box(Modifier.padding(top = 10.dp).fillMaxWidth().background(Color(0x1FFFFFFF)).padding(top = 1.dp))
        Text(example, color = Pal.Muted, fontSize = 14.4.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
fun GlossaryScreen(onBack: () -> Unit) {
    PlatformBackHandler(true, onBack)
    // Los ejemplos salen de CutCalculator: una sola fuente de verdad para las fórmulas.
    val down = CutCalculator.cutFor(52, 10)
    val up = CutCalculator.cutFor(3, 10)
    Screen {
        TopBar("‹ Volver", onBack, center = { BarTitle("Glosario de fórmulas") }, right = {})
        Text("Carta al número", color = Pal.Ink, fontFamily = Serif, fontSize = 25.6.sp, modifier = Modifier.padding(top = 22.dp))
        Text(
            buildAnnotatedString {
                append("Te piden una carta y una posición. Con un solo corte tienes que dejar esa carta en esa posición. Llama ")
                strong("C"); append(" a la posición actual de la carta y "); strong("D"); append(" a la posición de destino. El resultado ")
                strong("N"); append(" es la posición de la carta que usas como referencia para cortar.")
            },
            color = Pal.Muted, fontSize = 15.7.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 6.dp),
        )
        GlossaryCase(
            "Destino anterior a la carta", "D < C · corte abajo", Pal.Orange, "(cuando tienes que subir la carta)", "N = C − D",
            buildAnnotatedString {
                append("La carta tiene que subir. Resta directa: corta dejando "); strong("abajo del todo")
                append(" la carta que está en la posición N.")
            },
            buildAnnotatedString {
                append("Ejemplo: "); card(MnemonicaStack.cardAt(down.c)); append(" (C = "); strong(down.c.toString())
                append(") a la D = "); strong(down.dest.toString()); append(".\nN = ${down.c} − ${down.dest} = "); strong(down.n.toString())
                append(" → en la ${down.n} está el "); card(down.refCard); append(" → corta dejando el "); card(down.refCard); append(" abajo.")
            },
        )
        GlossaryCase(
            "Destino posterior a la carta", "D > C · corte arriba", Pal.Green, "(cuando tienes que bajar la carta)", "N = 53 − (D − C)",
            buildAnnotatedString {
                append("La carta tiene que bajar. Resta la diferencia y saca su complemento a 53: corta dejando ")
                strong("arriba del todo"); append(" la carta que está en la posición N.")
            },
            buildAnnotatedString {
                append("Ejemplo: "); card(MnemonicaStack.cardAt(up.c)); append(" (C = "); strong(up.c.toString())
                append(") a la D = "); strong(up.dest.toString()); append(".\nN = 53 − (${up.dest} − ${up.c}) = 53 − ${up.d} = "); strong(up.n.toString())
                append(" → en la ${up.n} está la "); card(up.refCard); append(" → corta dejando la "); card(up.refCard); append(" arriba.")
            },
        )
        Text(
            "Si D = C la carta ya está en su sitio: no hay que cortar.",
            color = Pal.Muted, fontSize = 14.4.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 14.dp),
        )
    }
}
