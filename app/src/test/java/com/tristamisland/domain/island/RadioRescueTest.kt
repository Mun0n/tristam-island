package com.tristamisland.domain.island

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioRescueTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun fourRightsAndFiveLeftsFindTheCoastGuard() {
        var state = at("comms_room")
        repeat(4) { state = go(state, "tune_right") }
        assertEquals(4100, state.radioFreq)
        assertEquals(0, state.score)
        repeat(4) { state = go(state, "tune_left") }
        assertEquals(4048, state.radioFreq)
        state = go(state, "tune_left")
        assertEquals(4035, state.radioFreq)
        assertEquals(5, state.score)
        assertTrue(state.lastResponse.orEmpty().contains("guardacostas"))
        assertTrue(state.journalIds.contains("radio_frequency"))
        state = go(state, "tune_left")
        assertEquals(4022, state.radioFreq)
        assertEquals(5, state.score)
        assertTrue(placePassage(story, state).contains("12.4022 Mc"))
    }

    @Test
    fun theKnobStopsAtBothEndsOfTheBand() {
        var state = at("comms_room")
        repeat(6) { state = go(state, "tune_right") }
        assertEquals(4126, state.radioFreq)
        assertTrue(state.lastResponse.orEmpty().contains("12.4126 Mc"))
        state = at("comms_room")
        repeat(10) { state = go(state, "tune_left") }
        assertEquals(3895, state.radioFreq)
        assertTrue(state.lastResponse.orEmpty().contains("12.3895 Mc"))
    }

    @Test
    fun theCallNeedsThePhonesTheCableAndTheMark() {
        val room = at("comms_room").copy(radioFreq = 4035, flags = at("comms_room").flags + ("freq_reached" to true))
        val quiet = go(room, "call_coast_guard")
        assertFalse(quiet.flags["rescued"] == true)
        assertEquals(0, quiet.score)
        val noisy = go(room.copy(flags = room.flags + ("headphones_on" to true)), "call_coast_guard")
        assertTrue(noisy.lastResponse.orEmpty().contains("no se oye nada"))
        assertEquals(0, noisy.score)
        val static = go(
            room.copy(
                radioFreq = 4020,
                flags = room.flags + ("headphones_on" to true) + ("cable_fixed" to true),
            ),
            "call_coast_guard",
        )
        assertTrue(static.lastResponse.orEmpty().contains("estática"))
        assertFalse(static.lastResponse.orEmpty().contains("Tristam"))
        assertEquals(0, static.score)
    }

    @Test
    fun theRightCallBringsTheHelicopter() {
        val ready = at("comms_room").copy(
            radioFreq = 4035,
            flags = at("comms_room").flags + ("headphones_on" to true) + ("cable_fixed" to true),
        )
        val home = go(ready, "call_coast_guard")
        assertEquals(5, home.score)
        assertTrue(home.flags["rescued"] == true)
        assertTrue(home.journalIds.contains("rescue"))
        assertTrue(home.lastResponse.orEmpty().contains("Bradley"))
        assertFalse(home.lastResponse.orEmpty().contains("Tristam"))
        assertFalse(engine.availableActions(home).any { it.id == "call_coast_guard" })
        assertTrue(placePassage(story, home).contains("La llamada ya ha salido"))
        val seen = go(ready.copy(flags = ready.flags + ("tristam_seen" to true)), "call_coast_guard")
        assertTrue(seen.lastResponse.orEmpty().contains("Tristam"))
    }

    @Test
    fun theHeadphonesComeOffBeforeTheDoor() {
        val wearing = go(at("comms_room"), "wear_headphones")
        assertTrue(wearing.flags["headphones_on"] == true)
        val door = engine.resolve(wearing, "comms_east") as IslandResolution.Rejected
        assertEquals(IslandRejection.REQUIREMENTS_NOT_MET, door.reason)
        val off = go(wearing, "remove_headphones", "comms_east")
        assertEquals("lab_lobby", off.locationId)
        assertFalse(off.flags["headphones_on"] == true)
    }

    private fun at(place: String): IslandState {
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = (engine.continuePrologue(state) as IslandResolution.Accepted).state
        }
        return state.copy(
            locationId = place,
            visitedLocationIds = state.visitedLocationIds + place,
            introducedActIds = state.introducedActIds + "act_1" + "act_2" + "act_3",
        )
    }

    private fun go(state: IslandState, vararg actions: String): IslandState {
        var current = state
        actions.forEach { id ->
            val resolved = engine.resolve(current, id)
            check(resolved is IslandResolution.Accepted) { "$id -> $resolved" }
            current = resolved.state
        }
        return current
    }
}
