package com.corte53

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.corte53.audio.SpeechEngine
import com.corte53.domain.PositionFilter
import com.corte53.domain.SessionBuilder
import com.corte53.ui.AppSettings
import com.corte53.ui.CutsScreen
import com.corte53.ui.DeckScreen
import com.corte53.ui.GlossaryScreen
import com.corte53.ui.ListenScreen
import com.corte53.ui.MenuScreen
import com.corte53.ui.ResultScreen
import com.corte53.ui.SettingsStore
import com.corte53.ui.SubtractionsScreen
import com.corte53.ui.SystemBarsEffect
import com.corte53.ui.TrainScreen
import com.corte53.ui.TrainSession

private sealed interface Dest {
    data object Menu : Dest
    data class Train(val session: TrainSession) : Dest
    data class Result(val session: TrainSession) : Dest
    data object Listen : Dest
    data object Cuts : Dest
    data object Subs : Dest
    data object Deck : Dest
    data object Gloss : Dest
}

/** Raíz común a las dos plataformas: carga los ajustes, los guarda al cambiarlos y navega entre pantallas. */
@Composable
fun Corte53Root(store: SettingsStore, speech: SpeechEngine) {
    var settings by remember { mutableStateOf(store.load()) }
    var dest by remember { mutableStateOf<Dest>(Dest.Menu) }
    fun update(s: AppSettings) { settings = s; store.save(s) }
    val menu = settings.menu

    fun newSession(positions: List<Int>): TrainSession {
        val list = SessionBuilder.list(positions, menu.random)
        return TrainSession(list, SessionBuilder.directions(list.size, menu.teach))
    }
    fun startSession() {
        val p = PositionFilter.positions(menu.from, menu.to, menu.filter)
        if (p.isNotEmpty()) dest = Dest.Train(newSession(p))
    }

    SystemBarsEffect()
    MaterialTheme {
        when (val d = dest) {
            Dest.Menu -> MenuScreen(
                menu, { update(settings.copy(menu = it)) },
                onStart = ::startSession, onListen = { dest = Dest.Listen }, onCuts = { dest = Dest.Cuts },
                onSubs = { dest = Dest.Subs }, onDeck = { dest = Dest.Deck }, onGloss = { dest = Dest.Gloss },
            )
            is Dest.Train -> TrainScreen(d.session, onQuit = { dest = Dest.Menu }, onFinish = { dest = Dest.Result(d.session) })
            is Dest.Result -> ResultScreen(
                d.session,
                onRetryFails = {
                    val fails = d.session.list.filterIndexed { k, _ -> d.session.res[k] == false }
                    // barajadas si el orden es aleatorio; si no, por posición
                    dest = Dest.Train(newSession(if (menu.random) fails else fails.sorted()))
                },
                onAgain = ::startSession, onMenu = { dest = Dest.Menu },
            )
            Dest.Listen -> ListenScreen(menu, settings.audio, { update(settings.copy(audio = it)) }, speech) { dest = Dest.Menu }
            Dest.Cuts -> CutsScreen(menu, settings.cuts, { update(settings.copy(cuts = it)) }, speech) { dest = Dest.Menu }
            Dest.Subs -> SubtractionsScreen(settings.subs, { update(settings.copy(subs = it)) }, speech) { dest = Dest.Menu }
            Dest.Deck -> DeckScreen { dest = Dest.Menu }
            Dest.Gloss -> GlossaryScreen { dest = Dest.Menu }
        }
    }
}
