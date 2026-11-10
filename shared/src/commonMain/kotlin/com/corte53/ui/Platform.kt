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
