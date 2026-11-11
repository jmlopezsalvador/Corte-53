package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun PracticeCard(title: String, text: String, sample: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().panel(20).clickable(onClick = onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Pal.Cream, fontFamily = Serif, fontSize = 26.sp)
            Text(text, color = Pal.CreamDim, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
            Text(sample, color = Pal.Salmon, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
        }
        Box(Modifier.size(44.dp).background(Pal.Red, CircleShape), contentAlignment = Alignment.Center) { Icon(Ic.Next, Pal.Cream, 24.dp) }
    }
}

/** Pestaña Practicar: cálculo mental con la baraja. */
@Composable
fun ColumnScope.PracticeTab(onCuts: () -> Unit, onSubs: () -> Unit) {
    Text("Practicar", color = Pal.Cream, fontFamily = Serif, fontSize = 32.sp, modifier = Modifier.padding(top = 18.dp))
    Text("Cálculo con la baraja, a tu ritmo o cantado.", color = Pal.CreamDim, fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp, bottom = 22.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PracticeCard("Cortes", "Te doy una carta y una posición: dime por dónde cortar para llevarla allí.", "N = 53 − (D − C)", onCuts)
        PracticeCard("Restas", "Diferencia entre dos posiciones o complemento a 53.", "53 − 17 = 36", onSubs)
    }
}
