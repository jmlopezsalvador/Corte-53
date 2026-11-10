package com.corte53.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Voz con el TextToSpeech de Android (offline si el móvil tiene la voz en español instalada). */
class AndroidSpeechEngine(context: Context) : SpeechEngine {
    private val ready = CompletableDeferred<Boolean>()
    private val pending = ConcurrentHashMap<String, (Throwable?) -> Unit>()
    private var counter = 0L
    private var voiceId: String? = null

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready.complete(status == TextToSpeech.SUCCESS)
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { pending.remove(utteranceId)?.invoke(null) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                pending.remove(utteranceId)?.invoke(SpeechException("Error del sintetizador de voz"))
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                // al cancelar, la corrutina ya está cancelada: solo se libera el registro
                pending.remove(utteranceId)?.invoke(CancellationException("interrumpido"))
            }
        })
    }

    private fun spanish(): List<Voice> = runCatching {
        tts.voices.orEmpty().filter { it.locale.language == "es" && !it.features.orEmpty().contains("notInstalled") }
    }.getOrDefault(emptyList())

    /** La elegida; si no, la mejor instalada en el móvil (España primero, mayor calidad); si no, ninguna. */
    private fun selected(): Voice? {
        val all = spanish()
        voiceId?.let { id -> all.firstOrNull { it.name == id }?.let { return it } }
        return all.filter { !it.isNetworkConnectionRequired }
            .sortedWith(compareByDescending<Voice> { it.locale.country == "ES" }.thenByDescending { it.quality })
            .firstOrNull()
    }

    private fun Voice.toSpeechVoice(): SpeechVoice {
        val variant = name.substringAfter("-x-", "").substringBefore("-")
        val place = locale.getDisplayName(Locale("es")).replaceFirstChar { it.uppercase() }
        return SpeechVoice(name, if (variant.isNotEmpty()) "$place · $variant" else place, isNetworkConnectionRequired)
    }

    /** Aplica la voz elegida y devuelve la que quedará activa (puede ser de red si no hay otra). */
    private fun apply(): Voice? {
        val v = selected()
        if (v != null) tts.setVoice(v) else tts.setLanguage(Locale("es", "ES"))
        return tts.voice?.takeIf { it.locale.language == "es" }
    }

    override suspend fun loadVoices(): List<SpeechVoice> {
        if (!ready.await()) return emptyList()
        return spanish().sortedWith(compareBy<Voice>({ it.isNetworkConnectionRequired }, { it.locale.country != "ES" }, { it.name }))
            .map { it.toSpeechVoice() }
    }

    override suspend fun activeVoice(): SpeechVoice? {
        if (!ready.await()) return null
        return apply()?.toSpeechVoice()
    }

    override fun setVoice(id: String?) { voiceId = id }

    override suspend fun speak(text: String, rate: Double) {
        if (!ready.await()) throw SpeechException("No hay motor de voz (Texto a voz) en este móvil")
        if (apply() == null) throw SpeechException("Falta la voz en español: instálala en Ajustes → Texto a voz")
        tts.setSpeechRate(rate.toFloat())   // el tono se conserva
        val id = "u${counter++}"
        suspendCancellableCoroutine { cont ->
            pending[id] = { error ->
                when {
                    error == null -> cont.resume(Unit)
                    error is CancellationException -> {}
                    else -> cont.resumeWithException(error)
                }
            }
            cont.invokeOnCancellation {
                pending.remove(id)
                tts.stop()
            }
            if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) {
                pending.remove(id)
                cont.resumeWithException(SpeechException("No se pudo iniciar la voz"))
            }
        }
    }

    override fun stop() { tts.stop() }
}
