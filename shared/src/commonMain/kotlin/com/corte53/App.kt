package com.corte53

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.corte53.audio.SpeechEngine
import com.corte53.domain.PositionFilter
import com.corte53.domain.SessionBuilder
import com.corte53.ui.AppSettings
import com.corte53.ui.BottomBar
import com.corte53.ui.CutsScreen
import com.corte53.ui.LearnTab
import com.corte53.ui.LibraryTab
import com.corte53.ui.ListenScreen
import com.corte53.ui.PlatformBackHandler
import com.corte53.ui.PracticeTab
import com.corte53.ui.ResultScreen
import com.corte53.ui.Screen
import com.corte53.ui.SettingsStore
import com.corte53.ui.SettingsTab
import com.corte53.ui.SubtractionsScreen
import com.corte53.ui.SystemBarsEffect
import com.corte53.ui.Tab
import com.corte53.ui.TrainScreen
import com.corte53.ui.TrainSession

/** Pantallas a pantalla completa por encima de las pestañas. */
private sealed interface Overlay {
    data class Train(val session: TrainSession) : Overlay
    data class Result(val session: TrainSession) : Overlay
    data object Listen : Overlay
    data object Cuts : Overlay
    data object Subs : Overlay
}

/** Raíz común a las dos plataformas: carga los ajustes, los guarda al cambiarlos y navega entre pestañas y pantallas. */
@Composable
fun Corte53Root(store: SettingsStore, speech: SpeechEngine) {
    var settings by remember { mutableStateOf(store.load()) }
    var tab by remember { mutableStateOf(Tab.Learn) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    fun update(s: AppSettings) { settings = s; store.save(s) }
    val menu = settings.menu

    fun newSession(positions: List<Int>): TrainSession {
        val list = SessionBuilder.list(positions, menu.random)
        return TrainSession(list, SessionBuilder.directions(list.size, menu.teach))
    }
    fun startSession() {
        val p = PositionFilter.positions(menu.from, menu.to, menu.filter)
        if (p.isNotEmpty()) overlay = Overlay.Train(newSession(p))
    }

    LaunchedEffect(settings.voiceId) { speech.setVoice(settings.voiceId) }
    SystemBarsEffect()
    MaterialTheme {
        when (val o = overlay) {
            null -> {
                // desde cualquier pestaña, atrás lleva a Memorizar; desde Memorizar sale de la app
                PlatformBackHandler(tab != Tab.Learn) { tab = Tab.Learn }
                Screen(scroll = tab != Tab.Library, bottomBar = { BottomBar(tab) { tab = it } }) {
                    when (tab) {
                        Tab.Learn -> LearnTab(
                            menu, { update(settings.copy(menu = it)) },
                            onStart = ::startSession, onListen = { overlay = Overlay.Listen },
                        )
                        Tab.Practice -> PracticeTab(onCuts = { overlay = Overlay.Cuts }, onSubs = { overlay = Overlay.Subs })
                        Tab.Library -> LibraryTab()
                        Tab.Settings -> SettingsTab(speech, settings.voiceId) { update(settings.copy(voiceId = it)) }
                    }
                }
            }
            is Overlay.Train -> TrainScreen(o.session, onQuit = { overlay = null }, onFinish = { overlay = Overlay.Result(o.session) })
            is Overlay.Result -> ResultScreen(
                o.session,
                onRetryFails = {
                    val fails = o.session.list.filterIndexed { k, _ -> o.session.res[k] == false }
                    // barajadas si el orden es aleatorio; si no, por posición
                    overlay = Overlay.Train(newSession(if (menu.random) fails else fails.sorted()))
                },
                onAgain = ::startSession, onMenu = { overlay = null },
            )
            Overlay.Listen -> ListenScreen(menu, settings.audio, { update(settings.copy(audio = it)) }, speech, settings.voiceId) { overlay = null }
            Overlay.Cuts -> CutsScreen(menu, settings.cuts, { update(settings.copy(cuts = it)) }, speech, settings.voiceId) { overlay = null }
            Overlay.Subs -> SubtractionsScreen(settings.subs, { update(settings.copy(subs = it)) }, speech, settings.voiceId) { overlay = null }
        }
    }
}
