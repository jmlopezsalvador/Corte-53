package com.corte53.ui

import com.corte53.domain.CardFilter
import com.corte53.domain.Rank
import com.corte53.domain.Suit
import com.corte53.domain.SubtractionMode
import com.corte53.domain.TeachMode

/** Los 4 grupos de ajustes de la web (`mnemo-cfg`, `mnemo-audio`, `mnemo-cortes`, `mnemo-restas`) con sus defectos. */

enum class FilterKind { ALL, SUIT, RANK }

data class MenuSettings(
    val random: Boolean = false,
    val teach: TeachMode = TeachMode.NUMBER,
    val from: Int = 1,
    val to: Int = 52,
    val filterKind: FilterKind = FilterKind.ALL,
    val suit: Suit = Suit.SPADES,
    val rank: Rank = Rank.EIGHT,
) {
    val filter: CardFilter
        get() = when (filterKind) {
            FilterKind.ALL -> CardFilter.All
            FilterKind.SUIT -> CardFilter.BySuit(suit)
            FilterKind.RANK -> CardFilter.ByRank(rank)
        }
}

/** [mode] = nombre de `SpokenSequence.ListenMode`. */
data class AudioSettings(
    val mode: String = "SAY",
    val gap: Double = 1.5,
    val inner: Double = 0.5,
    val rate: Double = 1.0,
    val loop: Boolean = false,
    val show: Boolean = true,
)

data class CutSettings(
    val voice: Boolean = false,
    val think: Double = 5.0,
    val step: Double = 0.75,
    val next: Double = 2.0,
    val rate: Double = 1.0,
)

data class SubtractionSettings(
    val mode: SubtractionMode = SubtractionMode.DIFF,
    val think: Double = 3.0,
    val gap: Double = 1.5,
    val rate: Double = 1.0,
)

data class AppSettings(
    val menu: MenuSettings = MenuSettings(),
    val audio: AudioSettings = AudioSettings(),
    val cuts: CutSettings = CutSettings(),
    val subs: SubtractionSettings = SubtractionSettings(),
)

/** Guarda los ajustes en el almacén de cada plataforma. Los decimales se guardan como milésimas (Long). */
class SettingsStore(private val prefs: KeyValueStore) {

    private inline fun <reified E : Enum<E>> enumOr(key: String, default: E): E =
        enumValues<E>().firstOrNull { it.name == prefs.getString(key) } ?: default

    private fun dbl(key: String, default: Double): Double = prefs.getLong(key, (default * 1000).toLong()) / 1000.0
    private fun putDbl(key: String, v: Double) = prefs.putLong(key, kotlin.math.round(v * 1000).toLong())

    fun load(): AppSettings {
        val m = MenuSettings()
        val a = AudioSettings()
        val c = CutSettings()
        val s = SubtractionSettings()
        return AppSettings(
            menu = MenuSettings(
                random = prefs.getBoolean("menu.random", m.random),
                teach = enumOr("menu.teach", m.teach),
                from = prefs.getLong("menu.from", m.from.toLong()).toInt().coerceIn(1, 52),
                to = prefs.getLong("menu.to", m.to.toLong()).toInt().coerceIn(1, 52),
                filterKind = enumOr("menu.filter", m.filterKind),
                suit = enumOr("menu.suit", m.suit),
                rank = enumOr("menu.rank", m.rank),
            ),
            audio = AudioSettings(
                mode = prefs.getString("audio.mode") ?: a.mode,
                gap = dbl("audio.gap", a.gap), inner = dbl("audio.inner", a.inner), rate = dbl("audio.rate", a.rate),
                loop = prefs.getBoolean("audio.loop", a.loop), show = prefs.getBoolean("audio.show", a.show),
            ),
            cuts = CutSettings(
                voice = prefs.getBoolean("cuts.voice", c.voice),
                think = dbl("cuts.think", c.think), step = dbl("cuts.step", c.step),
                next = dbl("cuts.next", c.next), rate = dbl("cuts.rate", c.rate),
            ),
            subs = SubtractionSettings(
                mode = enumOr("subs.mode", s.mode),
                think = dbl("subs.think", s.think), gap = dbl("subs.gap", s.gap), rate = dbl("subs.rate", s.rate),
            ),
        )
    }

    fun save(x: AppSettings) {
        with(x.menu) {
            prefs.putBoolean("menu.random", random); prefs.putString("menu.teach", teach.name)
            prefs.putLong("menu.from", from.toLong()); prefs.putLong("menu.to", to.toLong())
            prefs.putString("menu.filter", filterKind.name); prefs.putString("menu.suit", suit.name)
            prefs.putString("menu.rank", rank.name)
        }
        with(x.audio) {
            prefs.putString("audio.mode", mode); putDbl("audio.gap", gap); putDbl("audio.inner", inner)
            putDbl("audio.rate", rate); prefs.putBoolean("audio.loop", loop); prefs.putBoolean("audio.show", show)
        }
        with(x.cuts) {
            prefs.putBoolean("cuts.voice", voice); putDbl("cuts.think", think); putDbl("cuts.step", step)
            putDbl("cuts.next", next); putDbl("cuts.rate", rate)
        }
        with(x.subs) {
            prefs.putString("subs.mode", mode.name); putDbl("subs.think", think); putDbl("subs.gap", gap)
            putDbl("subs.rate", rate)
        }
    }
}
