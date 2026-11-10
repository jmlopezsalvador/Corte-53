package com.corte53

import androidx.compose.ui.window.ComposeUIViewController
import com.corte53.audio.IosSpeechEngine
import com.corte53.ui.SettingsStore
import com.corte53.ui.UserDefaultsStore
import platform.UIKit.UIViewController

/** Punto de entrada desde Swift: `MainViewControllerKt.MainViewController()`. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    Corte53Root(SettingsStore(UserDefaultsStore()), IosSpeechEngine())
}
