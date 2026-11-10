package com.corte53

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.corte53.audio.AndroidSpeechEngine
import com.corte53.ui.SettingsStore
import com.corte53.ui.SharedPrefsStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val store = SettingsStore(SharedPrefsStore(applicationContext))
        val speech = AndroidSpeechEngine(applicationContext)
        setContent { Corte53Root(store, speech) }
    }
}
