package com.tristamisland.domain.island

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CeilingAndRadioTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun theLockerTheCeilingAndTheCableOpenTheRadio() {
        var state = go(
            at("lab_think"),
            "take_chemistry_book",
            "think_north",
            "cross_north",
            "take_chair",
            "conference_east",
            "window_north",
            "look_lamp",
            "drop_chair",
            "put_chemistry_on_chair",
            "stand_on_chair",
            "push_with_book",
            "climb_with_book",
        )
        assertEquals("major_office", state.locationId)
        assertEquals(5, state.score)
        assertTrue(state.journalIds.contains("ceiling_office"))
        state = go(state, "search_bin_near")
        assertFalse(state.lastResponse.orEmpty().contains("Tristam"))
        assertTrue(state.journalIds.contains("grant_warning"))
        assertEquals(5, state.score)
        state = go(
            state,
            "open_drawer",
            "unlock_office_inside",
            "take_master_key",
            "office_south",
            "major_south",
            "window_south",
            "listen_lock",
            "open_locker",
            "take_cable",
        )
        assertEquals(15, state.score)
        val stairs = engine.resolve(state, "storage_stairs_keyed") as IslandResolution.Accepted
        assertEquals("storage", stairs.state.locationId)
        state = go(
            stairs.state,
            "storage_west",
            "cross_west",
            "hall_north",
            "unlock_comms",
            "enter_comms",
            "open_radio_panel",
            "replace_cable",
        )
        assertEquals("comms_room", state.locationId)
        assertEquals(20, state.score)
        assertFalse(state.inventory.contains("locker_cable"))
        assertTrue(state.journalIds.contains("radio_cable"))
        assertTrue(placePassage(story, state).contains("el cable nuevo ya está puesto"))
        assertFalse(engine.availableActions(state).any { it.id == "replace_cable" })
    }

    @Test
    fun thePhysicsBookCannotReachTheTile() {
        val state = go(
            at("lab_think"),
            "take_physics_book",
            "think_north",
            "cross_north",
            "take_chair",
            "conference_east",
            "window_north",
            "drop_chair",
            "put_physics_on_chair",
            "stand_on_chair",
            "reach_physics",
        )
        assertEquals(0, state.score)
        assertFalse(state.flags["tile_pushed"] == true)
        assertEquals("major_door", state.locationId)
    }

    @Test
    fun theFilingBoxAlsoReachesTheOffice() {
        val state = go(
            at("lab_lobby"),
            "take_filing_box",
            "lobby_south_hall",
            "hall_east",
            "cross_north",
            "take_chair",
            "conference_east",
            "window_north",
            "drop_chair",
            "put_filing_on_chair",
            "stand_on_chair",
            "push_with_box",
            "climb_with_box",
        )
        assertEquals("major_office", state.locationId)
        assertEquals(5, state.score)
        assertFalse(engine.availableActions(state).any { it.id == "climb_with_book" })
    }

    @Test
    fun aFullHandCannotTakeTheCable() {
        val storage = at("storage").copy(
            flags = at("storage").flags + ("locker_open" to true) + ("locker_unlocked" to true),
            inventory = listOf("matchbook", "fishing_rod", "flute"),
        )
        val rejected = engine.resolve(storage, "take_cable") as IslandResolution.Rejected
        assertEquals(IslandRejection.INVENTORY_FULL, rejected.reason)
        assertEquals(storage, rejected.state)
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
