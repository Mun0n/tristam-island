package com.tristamisland.presentation

import com.tristamisland.R

internal const val LOWER_ISLAND_REGION: String = "lower_island"
internal const val PLATEAU_REGION: String = "plateau"
internal const val MANSION_REGION: String = "mansion"
internal const val LABORATORY_REGION: String = "laboratory"

/** Reading level. Scene music stays inside the 0.15–0.25 range. */
internal const val MUSIC_READING_VOLUME: Float = 0.20f

/** Journal, inventory, and progress. Same track, a little quieter. */
internal const val MUSIC_PAGE_VOLUME: Float = 0.15f

internal enum class MusicTrack {
    ISLAND_ON_THE_HORIZON,
    LOWER_COAST,
    PLATEAU,
    MANSION,
    LABORATORY,
}

internal data class MusicCue(
    val track: MusicTrack?,
    val volume: Float,
)

/**
 * The menu and the chapter ask for a track from the current ambience.
 * A location change that keeps [musicRegion] keeps the same cue.
 * Silence stays silence when the listener has turned music off.
 */
internal fun musicCue(
    mode: IslandMode,
    page: IslandPage,
    musicRegion: String,
    enabled: Boolean,
): MusicCue {
    if (!enabled) return MusicCue(track = null, volume = 0f)
    if (mode != IslandMode.PLAY) {
        return MusicCue(MusicTrack.ISLAND_ON_THE_HORIZON, MUSIC_READING_VOLUME)
    }
    val track = when (musicRegion) {
        LOWER_ISLAND_REGION -> MusicTrack.LOWER_COAST
        PLATEAU_REGION -> MusicTrack.PLATEAU
        MANSION_REGION -> MusicTrack.MANSION
        LABORATORY_REGION -> MusicTrack.LABORATORY
        else -> null
    } ?: return MusicCue(track = null, volume = 0f)
    val volume = if (page == IslandPage.SCENE) MUSIC_READING_VOLUME else MUSIC_PAGE_VOLUME
    return MusicCue(track, volume)
}

internal val MusicTrack.label: String
    get() = when (this) {
        MusicTrack.ISLAND_ON_THE_HORIZON -> "island_on_the_horizon"
        MusicTrack.LOWER_COAST -> "lower_coast"
        MusicTrack.PLATEAU -> "plateau"
        MusicTrack.MANSION -> "mansion"
        MusicTrack.LABORATORY -> "laboratory"
    }

internal fun trackResource(track: MusicTrack): Int = when (track) {
    MusicTrack.ISLAND_ON_THE_HORIZON -> R.raw.island_on_the_horizon
    MusicTrack.LOWER_COAST -> R.raw.lower_coast
    MusicTrack.PLATEAU -> R.raw.plateau
    MusicTrack.MANSION -> R.raw.mansion
    MusicTrack.LABORATORY -> R.raw.laboratory
}
