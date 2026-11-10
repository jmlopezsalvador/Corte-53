package com.corte53

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.corte53.ui.SettingsStore
import com.corte53.ui.SystemBarsEffect

/** Raíz común a las dos plataformas: carga los ajustes, los guarda al cambiarlos y pinta la app. */
@Composable
fun Corte53Root(store: SettingsStore) {
    @Suppress("UNUSED_VARIABLE")
    var settings by remember { mutableStateOf(store.load()) }
    SystemBarsEffect()
    MaterialTheme {
        // Marcador de posición: las pantallas se irán añadiendo según 01-FUNCIONALIDADES.md.
        Box(Modifier.fillMaxSize().background(Color(0xFF0F3D2E)).safeDrawingPadding(), Alignment.Center) {
            Text("Mnemónica", color = Color(0xFFF3EFE3), fontSize = 40.sp, fontFamily = FontFamily.Serif)
        }
    }
}
