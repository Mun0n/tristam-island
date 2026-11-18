package com.tristamisland.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IslandMusicTest {
    @Test
    fun theMenuGuideAndCreditsShareTheHorizonTrack() {
        val horizon = MusicCue(MusicTrack.ISLAND_ON_THE_HORIZON, MUSIC_READING_VOLUME)
        assertEquals(horizon, musicCue(IslandMode.MENU, IslandPage.SCENE, "", enabled = true))
        assertEquals(horizon, musicCue(IslandMode.GUIDE, IslandPage.SCENE, LOWER_ISLAND_REGION, enabled = true))
        assertEquals(horizon, musicCue(IslandMode.CREDITS, IslandPage.JOURNAL, LOWER_ISLAND_REGION, enabled = true))
    }

    @Test
    fun theLowerIslandKeepsOneTrackAcrossTheChapterPages() {
        val scene = musicCue(IslandMode.PLAY, IslandPage.SCENE, LOWER_ISLAND_REGION, enabled = true)
        val journal = musicCue(IslandMode.PLAY, IslandPage.JOURNAL, LOWER_ISLAND_REGION, enabled = true)
        val inventory = musicCue(IslandMode.PLAY, IslandPage.INVENTORY, LOWER_ISLAND_REGION, enabled = true)
        val progress = musicCue(IslandMode.PLAY, IslandPage.STATUS, LOWER_ISLAND_REGION, enabled = true)
        assertEquals(MusicCue(MusicTrack.LOWER_COAST, MUSIC_READING_VOLUME), scene)
        assertEquals(MusicCue(MusicTrack.LOWER_COAST, MUSIC_PAGE_VOLUME), journal)
        assertEquals(journal, inventory)
        assertEquals(journal, progress)
        assertEquals(scene.track, journal.track)
    }

    @Test
    fun eachLaterActKeepsItsOwnTrack() {
        assertEquals(
            MusicCue(MusicTrack.PLATEAU, MUSIC_READING_VOLUME),
            musicCue(IslandMode.PLAY, IslandPage.SCENE, PLATEAU_REGION, enabled = true),
        )
        assertEquals(
            MusicCue(MusicTrack.MANSION, MUSIC_PAGE_VOLUME),
            musicCue(IslandMode.PLAY, IslandPage.JOURNAL, MANSION_REGION, enabled = true),
        )
        assertEquals(
            MusicCue(MusicTrack.LABORATORY, MUSIC_READING_VOLUME),
            musicCue(IslandMode.PLAY, IslandPage.SCENE, LABORATORY_REGION, enabled = true),
        )
    }

    @Test
    fun aDifferentRegionReleasesTheCoastTrack() {
        val cue = musicCue(IslandMode.PLAY, IslandPage.SCENE, "abandoned_village", enabled = true)
        assertNull(cue.track)
        assertEquals(0f, cue.volume)
    }

    @Test
    fun silenceStaysOffOnTheMenuAndInTheChapter() {
        assertNull(musicCue(IslandMode.MENU, IslandPage.SCENE, "", enabled = false).track)
        assertNull(musicCue(IslandMode.PLAY, IslandPage.SCENE, LOWER_ISLAND_REGION, enabled = false).track)
        assertNull(musicCue(IslandMode.PLAY, IslandPage.JOURNAL, LOWER_ISLAND_REGION, enabled = false).track)
    }
}
