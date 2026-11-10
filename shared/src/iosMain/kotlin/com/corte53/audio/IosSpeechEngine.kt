package com.corte53.audio

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMaximumSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMinimumSpeechRate
import platform.AVFAudio.setActive
import platform.darwin.NSObject
import kotlin.coroutines.resume

/** Voz con AVSpeechSynthesizer (offline; suena con el interruptor de silencio gracias a la categoría playback). */
class IosSpeechEngine : SpeechEngine {
    private val synthesizer = AVSpeechSynthesizer()
    private var voiceId: String? = null
    private var onFinished: (() -> Unit)? = null

    // El delegado es débil en iOS: lo guardamos en una propiedad para que no se libere.
    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            val cb = onFinished; onFinished = null; cb?.invoke()
        }
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didCancelSpeechUtterance: AVSpeechUtterance) {
            val cb = onFinished; onFinished = null; cb?.invoke()
        }
    }

    init {
        synthesizer.delegate = delegate
        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        }
    }

    override val voices: List<SpeechVoice>
        get() = AVSpeechSynthesisVoice.speechVoices()
            .filterIsInstance<AVSpeechSynthesisVoice>()
            .filter { it.language.startsWith("es") }
            .sortedBy { it.name }
            .map { SpeechVoice(it.identifier, "${it.name} (${it.language})") }

    override fun setVoice(id: String?) { voiceId = id }

    override suspend fun speak(text: String, rate: Double) {
        val voice = voiceId?.let { AVSpeechSynthesisVoice.voiceWithIdentifier(it) }
            ?: AVSpeechSynthesisVoice.voiceWithLanguage("es-ES")
            ?: throw SpeechException("Falta la voz en español: instálala en Ajustes → Accesibilidad → Contenido hablado")
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = voice
        val scaled = AVSpeechUtteranceDefaultSpeechRate * rate.toFloat()
        utterance.rate = scaled.coerceIn(AVSpeechUtteranceMinimumSpeechRate, AVSpeechUtteranceMaximumSpeechRate)
        suspendCancellableCoroutine { cont ->
            onFinished = { if (cont.isActive) cont.resume(Unit) }
            cont.invokeOnCancellation {
                onFinished = null
                synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
            }
            synthesizer.speakUtterance(utterance)
        }
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}
