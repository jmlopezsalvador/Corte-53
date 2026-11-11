package com.corte53.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SpeechException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

/**
 * Pantalla de trabajo (escuchar, cortes, restas): barra superior con acciones, contenido y controles fijos abajo.
 * Atrás del sistema = volver.
 */
@Composable
fun WorkScreen(
    title: String, onBack: () -> Unit,
    trailing: @Composable RowScope.() -> Unit = {},
    scroll: Boolean = true,
    controls: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlatformBackHandler(true, onBack)
    Screen(scroll = false) {
        TopBar(title, onBack, trailing)
        val body = Modifier.weight(1f).fillMaxWidth()
        Column(if (scroll) body.verticalScroll(rememberScrollState()) else body, content = content)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            controls()
        }
    }
}

/** Contador de la barra superior («Nº 3», «4 / 52»). */
@Composable
fun BarCounter(text: String) {
    Text(text, color = Pal.CreamDim, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
}

/**
 * Lanza y para la locución de una pantalla. Un solo trabajo a la vez; parar lo cancela
 * (la voz se corta sola por la cancelación estructurada) y avisa si el motor falla.
 */
class VoiceController(private val scope: CoroutineScope) {
    var on by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var job: Job? = null

    fun start(block: suspend () -> Unit) {
        job?.cancel()
        error = null
        on = true
        val me = scope.launch(start = CoroutineStart.LAZY) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SpeechException) {
                error = e.message ?: "No se pudo reproducir la voz"
            } finally {
                if (job === currentCoroutineContext()[Job]) on = false
            }
        }
        job = me
        me.start()
    }

    fun stop() {
        job?.cancel()
        job = null
        on = false
    }
}

@Composable
fun rememberVoice(): VoiceController {
    val scope = rememberCoroutineScope()
    val controller = remember { VoiceController(scope) }
    OnAppStopped { controller.stop() }   // al salir de la app se para la voz
    return controller
}
