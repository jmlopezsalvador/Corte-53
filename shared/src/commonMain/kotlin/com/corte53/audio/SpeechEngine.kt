package com.corte53.audio

/** Voz del sistema disponible para hablar en español. */
data class SpeechVoice(val id: String, val name: String)

/** Falla al hablar (sin voz en español, motor no disponible…). */
class SpeechException(message: String) : Exception(message)

/**
 * Motor de voz. La implementación nativa (TTS de Android, AVSpeechSynthesizer de iOS) vive en
 * androidMain / iosMain; en los tests se usa uno falso.
 */
interface SpeechEngine {
    /** Voces en español instaladas (vacía si aún no se sabe). */
    val voices: List<SpeechVoice>

    /** Elige la voz por su [SpeechVoice.id]; null = la de por defecto del sistema. */
    fun setVoice(id: String?)

    /**
     * Dice [text] a velocidad [rate] (1,0 = normal, tono conservado) y vuelve al terminar.
     * Si la corrutina se cancela, la locución se corta. Lanza [SpeechException] si no puede hablar.
     */
    suspend fun speak(text: String, rate: Double)

    /** Corta cualquier locución en curso. */
    fun stop()
}
