package com.corte53.ui

import androidx.compose.runtime.Composable
import platform.Foundation.NSUserDefaults

// En iOS la barra de estado la gestiona SwiftUI con .preferredColorScheme; no hay nada que ajustar.
@Composable
actual fun SystemBarsEffect() {}

/** Ajustes guardados en NSUserDefaults. */
class UserDefaultsStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun getString(key: String): String? = defaults.stringForKey(key)
    override fun getBoolean(key: String, default: Boolean) =
        if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)
    override fun getLong(key: String, default: Long) =
        if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key)
    override fun putString(key: String, value: String) = defaults.setObject(value, forKey = key)
    override fun putBoolean(key: String, value: Boolean) = defaults.setBool(value, forKey = key)
    override fun putLong(key: String, value: Long) = defaults.setInteger(value, forKey = key)
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@Composable
actual fun KeepScreenOn(on: Boolean) {
    androidx.compose.runtime.DisposableEffect(on) {
        platform.UIKit.UIApplication.sharedApplication.idleTimerDisabled = on
        onDispose { platform.UIKit.UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}

// Pendiente al retomar iOS: observar UIApplicationDidEnterBackgroundNotification.
@Composable
actual fun OnAppStopped(onStopped: () -> Unit) {}
