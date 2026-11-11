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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SpeechEngine
import com.corte53.audio.SpeechException
import com.corte53.audio.SpeechVoice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val SAMPLE = "Siete de diamantes, a la posición cuarenta y ocho"

/** Estado de la voz: las ofrecidas y la que sonará. Se recalcula al cambiar la elección. */
class VoiceInfo(val voices: List<SpeechVoice>, val active: SpeechVoice?)

@Composable
fun rememberVoiceInfo(speech: SpeechEngine, voiceId: String?): VoiceInfo? {
    var info by remember { mutableStateOf<VoiceInfo?>(null) }
    LaunchedEffect(speech, voiceId) {
        speech.setVoice(voiceId)
        info = VoiceInfo(speech.loadVoices(), speech.activeVoice())
    }
    return info
}

/** Aviso si la voz necesita internet o no hay voz en español; nada si todo va bien. */
@Composable
fun VoiceWarning(speech: SpeechEngine, voiceId: String?) {
    val info = rememberVoiceInfo(speech, voiceId) ?: return
    when {
        info.active == null -> Warning("No hay voz en español en este móvil. Instálala en Ajustes, Texto a voz.")
        info.active.network -> Warning("La voz actual usa internet y sin conexión no sonará. Elige otra en Ajustes.")
    }
}

@Composable
private fun VoiceRow(name: String, detail: String, warn: Boolean, selected: Boolean, onSelect: () -> Unit, onPlay: (() -> Unit)?, playing: Boolean) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(24.dp).border(2.dp, if (selected) Pal.Cream else Pal.Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) { if (selected) Box(Modifier.size(12.dp).background(Pal.Cream, CircleShape)) }
        Column(Modifier.weight(1f)) {
            Text(name, color = Pal.Cream, fontSize = 17.sp)
            Text(detail, color = if (warn) Pal.Amber else Pal.CreamDim, fontSize = 13.sp)
        }
        if (onPlay != null) {
            Box(
                Modifier.size(44.dp).border(1.dp, Pal.Line, CircleShape).clickable(onClick = onPlay)
                    .semantics { contentDescription = if (playing) "Parar" else "Escuchar esta voz" },
                contentAlignment = Alignment.Center,
            ) { Icon(if (playing) Ic.Pause else Ic.Play, Pal.Cream, 20.dp) }
        }
    }
}

/** Pestaña Ajustes: elegir voz y oírla. */
@Composable
fun ColumnScope.SettingsTab(speech: SpeechEngine, voiceId: String?, onVoice: (String?) -> Unit) {
    val info = rememberVoiceInfo(speech, voiceId)
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var playing by remember { mutableStateOf<String?>(null) }   // id de la voz que suena ("" = la automática)
    var error by remember { mutableStateOf<String?>(null) }

    fun play(id: String?) {
        val key = id ?: ""
        job?.cancel()
        if (playing == key) { playing = null; return }
        error = null; playing = key
        job = scope.launch {
            try {
                speech.setVoice(id)
                speech.speak(SAMPLE, 1.0)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SpeechException) {
                error = e.message
            } finally {
                speech.setVoice(voiceId)   // se vuelve a la voz elegida
                if (playing == key) playing = null
            }
        }
    }

    Text("Ajustes", color = Pal.Cream, fontFamily = Serif, fontSize = 32.sp, modifier = Modifier.padding(top = 18.dp))
    SectionTitle("Voz")
    when {
        info == null -> Hint("Buscando voces…")
        info.voices.isEmpty() -> Warning("No hay voz en español en este móvil. Instálala en Ajustes, Texto a voz.")
        else -> {
            Text("Se muestran las voces en español de tu país. Toca el botón de cada una para oírla.", color = Pal.CreamDim, fontSize = 14.sp, lineHeight = 20.sp)
            val auto = info.active?.takeIf { voiceId == null }
            Column(Modifier.padding(top = 8.dp)) {
                VoiceRow(
                    "Automática", if (voiceId == null && auto != null) "Ahora suena ${auto.name}" else "La mejor voz instalada en el móvil",
                    false, voiceId == null, { onVoice(null) }, { play(null) }, playing == "",
                )
                info.voices.forEach { v ->
                    VoiceRow(
                        v.name, if (v.network) "Necesita internet" else "Instalada, funciona sin conexión", v.network,
                        voiceId == v.id, { onVoice(v.id) }, { play(v.id) }, playing == v.id,
                    )
                }
            }
            if (info.voices.all { it.network }) Warning("Ninguna voz está instalada: sin conexión no sonará. Descarga una en Ajustes, Texto a voz.")
        }
    }
    error?.let { Warning(it) }

    SectionTitle("Acerca de")
    Wordmark(26f)
    Text("Entrenador de la Mnemónica de Juan Tamariz.", color = Pal.CreamDim, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
    Text("Funciona sin conexión. Tus ajustes se guardan solo en este móvil.", color = Pal.CreamDim, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
}
