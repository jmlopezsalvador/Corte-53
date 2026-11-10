package com.corte53.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    override val voices: List<SpeechVoice>
        get() = runCatching {
            tts.voices.orEmpty()
                .filter { it.locale.language == "es" && !it.isNetworkConnectionRequired }
                .sortedBy { it.name }
                .map { SpeechVoice(it.name, "${it.name} (${it.locale.displayCountry.ifBlank { it.locale.language }})") }
        }.getOrDefault(emptyList())

    override fun setVoice(id: String?) { voiceId = id }

    override suspend fun speak(text: String, rate: Double) {
        if (!ready.await()) throw SpeechException("No hay motor de voz (Texto a voz) en este móvil")
        val voice = voiceId?.let { id -> runCatching { tts.voices.firstOrNull { it.name == id } }.getOrNull() }
        val langResult = if (voice != null) tts.setVoice(voice) else tts.setLanguage(Locale("es", "ES"))
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            throw SpeechException("Falta la voz en español: instálala en Ajustes → Texto a voz")
        }
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
