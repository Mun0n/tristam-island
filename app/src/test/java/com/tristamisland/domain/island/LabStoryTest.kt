package com.tristamisland.domain.island

import com.tristamisland.data.IslandRecord
import com.tristamisland.presentation.IslandSnapshot
import com.tristamisland.presentation.presentIsland
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabStoryTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun theElevatorNamesTheBombAndTheLetterDatesIt() {
        val cabin = at("antechamber").copy(
            flags = at("antechamber").flags + ("tristam_seen" to true),
        )
        var state = go(cabin, "enter_elevator", "descend")
        assertEquals("lab_lobby", state.locationId)
        assertEquals(5, state.score)
        assertTrue(state.journalIds.contains("lab_descent"))
        val card = presentIsland(story, engine, IslandSnapshot(record = IslandRecord(state, revision = 1)))
        assertEquals("Acto 4", card.actCard)
        assertTrue(placePassage(story, state.copy(locationId = "inside_elevator")).contains("vestíbulo del laboratorio"))
        val again = engine.resolve(
            state.copy(locationId = "inside_elevator"),
            "descend",
        ) as IslandResolution.Rejected
        assertEquals(IslandRejection.ALREADY_RESOLVED, again.reason)
        state = go(state, "read_poster_tristam")
        assertEquals(10, state.score)
        assertTrue(state.journalIds.contains("nuclear_poster"))
        assertFalse(engine.availableActions(state).any { it.id == "read_poster" })
        state = go(
            state,
            "lobby_south_hall",
            "hall_south",
            "look_geiger",
            "bench_east",
            "look_blackboard",
            "think_north",
            "cross_north",
            "read_letter_tristam",
        )
        assertEquals(10, state.score)
        assertTrue(state.journalIds.contains("grant_warning"))
        state = go(state, "conference_east", "look_hangar", "window_south")
        val locked = engine.resolve(state, "storage_lock") as IslandResolution.Accepted
        assertFalse(locked.state.flags["locker_open"] == true)
        state = go(locked.state, "storage_north", "window_north", "major_locked")
        assertEquals("major_door", state.locationId)
        state = go(state, "major_south", "window_west", "conference_south", "cross_west", "hall_north", "comms_locked")
        assertEquals("lab_lobby", state.locationId)
        val back = go(state, "lobby_to_elevator")
        val title = presentIsland(story, engine, IslandSnapshot(record = IslandRecord(back, revision = 1)))
        assertEquals("El laboratorio", title.actTitle)
    }

    @Test
    fun thePosterWithoutTristamDoesNotNameTheCrater() {
        val lobby = at("lab_lobby").copy(flags = at("lab_lobby").flags + ("elevator_down" to true))
        val state = go(lobby, "read_poster")
        val poster = story.journal.first { it.id == "nuclear_poster_near" }.text
        assertTrue(state.journalIds.contains("nuclear_poster_near"))
        assertFalse(poster.contains("Tristam"))
        assertFalse(state.lastResponse.orEmpty().contains("Tristam"))
        assertEquals(5, state.score)
        assertFalse(engine.availableActions(state).any { it.id == "read_poster_tristam" })
        val letter = go(state.copy(locationId = "conference"), "read_letter")
        assertFalse(letter.lastResponse.orEmpty().contains("Tristam"))
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
