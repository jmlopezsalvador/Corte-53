package com.corte53.ui

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun SystemBarsEffect() {
    val activity = LocalContext.current as ComponentActivity
    LaunchedEffect(Unit) {
        val bars = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        activity.enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
    }
}

/** Ajustes guardados en SharedPreferences. */
class SharedPrefsStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("corte53_settings", Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun getLong(key: String, default: Long) = prefs.getLong(key, default)
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    override fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
}
