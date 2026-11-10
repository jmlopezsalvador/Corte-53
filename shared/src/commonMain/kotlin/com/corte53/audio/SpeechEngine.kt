package com.corte53.audio

/** Voz del sistema en español. [network] = necesita internet para sonar. */
data class SpeechVoice(val id: String, val name: String, val network: Boolean = false)

/** Falla al hablar (sin voz en español, motor no disponible…). */
class SpeechException(message: String) : Exception(message)

/**
 * Motor de voz. La implementación nativa (TTS de Android, AVSpeechSynthesizer de iOS) vive en
 * androidMain / iosMain; en los tests se usa uno falso.
 */
interface SpeechEngine {
    /** Voces en español del sistema (espera a que el motor esté listo). Vacía si no hay ninguna. */
    suspend fun loadVoices(): List<SpeechVoice>

    /** La voz que sonará ahora (la elegida, o la automática), o null si no hay voz en español. */
    suspend fun activeVoice(): SpeechVoice?

    /** Elige la voz por su [SpeechVoice.id]; null = automática (prefiere una instalada en el móvil). */
    fun setVoice(id: String?)

    /**
     * Dice [text] a velocidad [rate] (1,0 = normal, tono conservado) y vuelve al terminar.
     * Si la corrutina se cancela, la locución se corta. Lanza [SpeechException] si no puede hablar.
     */
    suspend fun speak(text: String, rate: Double)

    /** Corta cualquier locución en curso. */
    fun stop()
}
