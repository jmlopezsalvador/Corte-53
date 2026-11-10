package com.corte53.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corte53.audio.SpeechEngine
import com.corte53.audio.SpeechException
import com.corte53.audio.SpeechVoice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val SAMPLE = "Siete de diamantes, a la posición cuarenta y ocho"

/** Estado de la voz que sonará: se recalcula al cambiar la elección. */
private class VoiceInfo(val voices: List<SpeechVoice>, val active: SpeechVoice?)

@Composable
private fun rememberVoiceInfo(speech: SpeechEngine, voiceId: String?): VoiceInfo? {
    var info by remember { mutableStateOf<VoiceInfo?>(null) }
    LaunchedEffect(speech, voiceId) {
        speech.setVoice(voiceId)
        info = VoiceInfo(speech.loadVoices(), speech.activeVoice())
    }
    return info
}

/** Aviso naranja si la voz necesita internet o no hay voz en español; nada si todo va bien. */
@Composable
fun VoiceWarning(speech: SpeechEngine, voiceId: String?) {
    val info = rememberVoiceInfo(speech, voiceId) ?: return
    val msg = when {
        info.active == null -> "No hay voz en español en este móvil. Instálala en Ajustes → Texto a voz."
        info.active.network -> "La voz «${info.active.name}» usa internet: sin conexión no sonará. Elige otra en el menú."
        else -> return
    }
    Text(msg, color = Pal.Orange, fontSize = 13.6.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
}

/** Sección «Voz» del menú: elegir voz, ver si es local o de red y probarla. */
@Composable
fun VoiceSection(speech: SpeechEngine, voiceId: String?, onVoice: (String?) -> Unit) {
    val info = rememberVoiceInfo(speech, voiceId)
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var testing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var open by remember { mutableStateOf(false) }

    Label("Voz")
    val chosen = info?.voices?.firstOrNull { it.id == voiceId }
    val shown = when {
        info == null -> "…"
        voiceId == null -> "Automática" + (info.active?.let { " · ${it.name}" } ?: "")
        else -> chosen?.name ?: "Automática"
    }
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().background(Color(0x40000000), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp))
                .clickable(enabled = info != null) { open = true }.padding(14.dp),
            contentAlignment = Alignment.CenterStart,
        ) { Text("$shown  ▾", color = Pal.Ink, fontSize = 15.2.sp) }
        DropdownMenu(open, { open = false }, modifier = Modifier.background(Pal.Felt)) {
            DropdownMenuItem(
                text = { Text("Automática (instalada en el móvil)", color = Pal.Ink) },
                onClick = { onVoice(null); open = false },
            )
            info?.voices?.forEach { v ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(v.name, color = Pal.Ink)
                            Text(if (v.network) "Necesita internet" else "Instalada · sin conexión", color = if (v.network) Pal.Orange else Pal.Muted, fontSize = 12.sp)
                        }
                    },
                    onClick = { onVoice(v.id); open = false },
                )
            }
        }
    }
    val active = info?.active
    when {
        info == null -> {}
        active == null -> Text("No hay voz en español en este móvil. Instálala en Ajustes → Texto a voz.", color = Pal.Orange, fontSize = 13.6.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
        active.network -> Text("Esta voz usa internet: sin conexión no sonará. Elige una instalada en el móvil.", color = Pal.Orange, fontSize = 13.6.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
        else -> Hint("Voz instalada en el móvil: funciona sin conexión.")
    }
    error?.let { Text(it, color = Pal.Orange, fontSize = 13.6.sp, modifier = Modifier.padding(top = 8.dp)) }
    GhostButton(
        if (testing) "❚❚ Parar" else "▶ Probar voz", Modifier.fillMaxWidth().padding(top = 10.dp), enabled = active != null,
    ) {
        if (testing) { job?.cancel(); testing = false; return@GhostButton }
        error = null; testing = true
        val me = scope.launch {
            try {
                speech.speak(SAMPLE, 1.0)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SpeechException) {
                error = e.message
            } finally {
                testing = false
            }
        }
        job = me
    }
}
