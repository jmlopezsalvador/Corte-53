package com.corte53.ui

import androidx.compose.runtime.Composable

/** Cosas que dependen del sistema operativo: cada plataforma las implementa en androidMain / iosMain. */

/** Pone las barras del sistema (estado/navegación) con iconos claros sobre el tapete oscuro. */
@Composable
expect fun SystemBarsEffect()

/** Almacén clave-valor de cada plataforma (SharedPreferences en Android, NSUserDefaults en iOS). */
interface KeyValueStore {
    fun getString(key: String): String?
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getLong(key: String, default: Long): Long
    fun putString(key: String, value: String)
    fun putBoolean(key: String, value: Boolean)
    fun putLong(key: String, value: Long)
}

/** Atrás del sistema (Android). En iOS no hace nada: la navegación va con los botones de la app. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Mantiene la pantalla encendida mientras [on] es true (mientras suena la voz). */
@Composable
expect fun KeepScreenOn(on: Boolean)

/** Avisa cuando la app deja de estar a la vista (segundo plano o pantalla bloqueada), para parar la voz. */
@Composable
expect fun OnAppStopped(onStopped: () -> Unit)
