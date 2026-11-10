package com.corte53

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SequenceRunner
import com.corte53.audio.SpeechEngine
import com.corte53.audio.SpeechException
import com.corte53.domain.CutCalculator
import com.corte53.domain.SpokenSequence
import com.corte53.ui.SettingsStore
import com.corte53.ui.SystemBarsEffect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Raíz común a las dos plataformas: carga los ajustes, los guarda al cambiarlos y pinta la app. */
@Composable
fun Corte53Root(store: SettingsStore, speech: SpeechEngine) {
    @Suppress("UNUSED_VARIABLE")
    var settings by remember { mutableStateOf(store.load()) }
    SystemBarsEffect()
    MaterialTheme {
        // Marcador de posición: las pantallas se irán añadiendo según 01-FUNCIONALIDADES.md.
        // El botón es una prueba temporal de la voz del sistema (se quitará con la pantalla de Cortes).
        val scope = rememberCoroutineScope()
        val runner = remember(speech) { SequenceRunner(speech) }
        var job by remember { mutableStateOf<Job?>(null) }
        var message by remember { mutableStateOf("") }
        Column(
            Modifier.fillMaxSize().background(Color(0xFF0F3D2E)).safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Mnemónica", color = Color(0xFFF3EFE3), fontSize = 40.sp, fontFamily = FontFamily.Serif)
            Button(onClick = {
                job?.cancel()
                message = ""
                job = scope.launch {
                    try {
                        val cut = CutCalculator.cutFor(3, 48)
                        runner.run(SpokenSequence.cutFull(cut, think = 2.0, step = 0.75), rate = 1.0) { message = it }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: SpeechException) {
                        message = e.message ?: "Error de voz"
                    }
                }
            }) { Text("Probar voz (corte 3 → 48)") }
            Text(message, color = Color(0xFFB7C8BE))
        }
    }
}
