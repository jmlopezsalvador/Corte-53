package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** «7♦» en rojo claro si el palo es rojo (fondo oscuro). */
fun AnnotatedString.Builder.card(c: Card) {
    withStyle(SpanStyle(color = if (c.suit.red) Pal.Salmon else Color.Unspecified, fontWeight = FontWeight.SemiBold)) { append(c.shortName) }
}

/** Resultado destacado. */
fun AnnotatedString.Builder.num(x: Any) =
    withStyle(SpanStyle(color = Pal.Salmon, fontWeight = FontWeight.Bold)) { append(x.toString()) }

fun AnnotatedString.Builder.hint(text: String) =
    withStyle(SpanStyle(color = Pal.Rose, fontStyle = FontStyle.Italic)) { append(text) }

fun AnnotatedString.Builder.strong(text: String) =
    withStyle(SpanStyle(color = Pal.Cream, fontWeight = FontWeight.Bold)) { append(text) }

/** Pestaña Baraja: las 52 cartas en orden y el glosario de fórmulas. */
@Composable
fun ColumnScope.LibraryTab() {
    var section by remember { mutableStateOf(0) }
    Text("Baraja", color = Pal.Cream, fontFamily = Serif, fontSize = 32.sp, modifier = Modifier.padding(top = 18.dp, bottom = 14.dp))
    Segmented(listOf(0 to "Las 52 cartas", 1 to "Fórmulas"), section, { section = it })
    if (section == 0) Deck(Modifier.weight(1f)) else Glossary(Modifier.weight(1f))
}

@Composable
private fun Deck(modifier: Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val w = (maxWidth - 36.dp - 8.dp) / 4
        LazyVerticalGrid(
            GridCells.Fixed(4), Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items((1..52).toList()) { p ->
                Box(Modifier.padding(top = 8.dp, end = 8.dp)) {
                    PlayingCard(MnemonicaStack.cardAt(p), w)
                    Box(
                        Modifier.align(Alignment.TopEnd).offset(8.dp, (-8).dp).widthIn(min = 26.dp)
                            .background(Pal.Badge, CircleShape).border(2.dp, Pal.Wine, CircleShape).padding(horizontal = 4.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(p.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormulaCase(
    title: String, tag: String, tagColor: Color, move: String, formula: String, body: AnnotatedString, example: AnnotatedString,
) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).panel().padding(18.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Pal.Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                tag, color = tagColor, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.background(Pal.SurfaceHi, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
        Text(move, color = Pal.Rose, fontStyle = FontStyle.Italic, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        Text(formula, color = Pal.Salmon, fontFamily = Serif, fontSize = 26.sp, modifier = Modifier.padding(top = 14.dp))
        Text(body, color = Pal.Cream, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
        Box(Modifier.padding(top = 14.dp).fillMaxWidth().background(Pal.Line).padding(top = 1.dp))
        Text(example, color = Pal.CreamDim, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun Glossary(modifier: Modifier) {
    // Los ejemplos salen de CutCalculator: una sola fuente de verdad para las fórmulas.
    val down = CutCalculator.cutFor(52, 10)
    val up = CutCalculator.cutFor(3, 10)
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Text("Carta al número", color = Pal.Cream, fontFamily = Serif, fontSize = 24.sp, modifier = Modifier.padding(top = 22.dp))
        Text(
            buildAnnotatedString {
                append("Te piden una carta y una posición. Con un solo corte tienes que dejar esa carta en esa posición. Llama ")
                strong("C"); append(" a la posición actual de la carta y "); strong("D"); append(" a la posición de destino. El resultado ")
                strong("N"); append(" es la posición de la carta que usas como referencia para cortar.")
            },
            color = Pal.CreamDim, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp),
        )
        FormulaCase(
            "Destino anterior a la carta", "D < C, corte abajo", Pal.Amber, "(cuando tienes que subir la carta)", "N = C − D",
            buildAnnotatedString {
                append("La carta tiene que subir. Resta directa: corta dejando "); strong("abajo del todo")
                append(" la carta que está en la posición N.")
            },
            buildAnnotatedString {
                append("Ejemplo: "); card(MnemonicaStack.cardAt(down.c)); append(" (C = "); strong(down.c.toString())
                append(") a la D = "); strong(down.dest.toString()); append(".\nN = ${down.c} − ${down.dest} = "); num(down.n)
                append(" → en la ${down.n} está el "); card(down.refCard); append(" → corta dejando el "); card(down.refCard); append(" abajo.")
            },
        )
        FormulaCase(
            "Destino posterior a la carta", "D > C, corte arriba", Pal.Green, "(cuando tienes que bajar la carta)", "N = 53 − (D − C)",
            buildAnnotatedString {
                append("La carta tiene que bajar. Resta la diferencia y saca su complemento a 53: corta dejando ")
                strong("arriba del todo"); append(" la carta que está en la posición N.")
            },
            buildAnnotatedString {
                append("Ejemplo: "); card(MnemonicaStack.cardAt(up.c)); append(" (C = "); strong(up.c.toString())
                append(") a la D = "); strong(up.dest.toString()); append(".\nN = 53 − (${up.dest} − ${up.c}) = 53 − ${up.d} = "); num(up.n)
                append(" → en la ${up.n} está la "); card(up.refCard); append(" → corta dejando la "); card(up.refCard); append(" arriba.")
            },
        )
        Text(
            "Si D = C la carta ya está en su sitio: no hay que cortar.",
            color = Pal.CreamDim, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 16.dp),
        )
    }
}
