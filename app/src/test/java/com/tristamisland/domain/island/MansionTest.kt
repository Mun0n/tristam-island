package com.tristamisland.domain.island

import com.tristamisland.data.IslandRecord
import com.tristamisland.presentation.IslandSnapshot
import com.tristamisland.presentation.presentIsland
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MansionTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun theCodeOpensTheMansionAndTheContraptionOpensTheLaboratoryDoor() {
        val porch = at("white_house_porch").copy(
            inventory = listOf("screwdriver"),
            flags = at("white_house_porch").flags + mapOf(
                "generators_on" to true,
                "code_read" to true,
            ),
        )
        var state = go(porch, "enter_code")
        assertEquals("white_house_lobby", state.locationId)
        assertEquals(5, state.score)
        assertTrue(state.journalIds.contains("mansion_purpose"))
        val card = presentIsland(story, engine, IslandSnapshot(record = IslandRecord(state, revision = 1)))
        assertEquals("Acto 3", card.actCard)
        val placards = state.score
        state = go(state, "read_placards")
        assertEquals(placards + 1, state.score)
        state = go(state, "lobby_up", "mezzanine_east", "unlock_yellow_first", "enter_yellow", "take_ball", "leave_yellow")
        val doors = state.score
        state = go(state, "unlock_white_later", "enter_white", "take_pump", "leave_white", "quarters_west", "mezzanine_down")
        assertEquals(doors, state.score)
        state = go(state, "plug_pump", "pump_air", "load_ball", "pull_lever_statue")
        assertEquals(doors + 2, state.score)
        assertTrue(state.flags["ball_on_lobby"] == true)
        assertFalse("cricket_ball" in state.inventory)
        state = go(state, "take_ball_lobby", "turn_statue", "pump_air", "load_ball", "pull_lever_break", "pump_air")
        val blocked = go(state, "pull_lever_blocked")
        assertTrue(blocked.flags["pump_pumped"] == true)
        assertEquals(state.score, blocked.score)
        state = go(blocked, "lobby_up", "take_ball_mezzanine", "take_screw", "fix_pipe", "mezzanine_down")
        assertFalse("pipe_screw" in state.inventory)
        assertTrue(state.flags["pipe_fixed"] == true)
        state = go(state, "load_ball", "pull_lever_rat")
        assertEquals(doors + 4, state.score)
        state = go(state, "take_rat_solves")
        assertEquals(doors + 9, state.score)
        assertTrue(state.journalIds.contains("laboratory_door"))
        assertTrue(placePassage(story, state).contains("la puerta está abierta"))
        state = go(state, "lobby_north", "enter_elevator")
        assertEquals("inside_elevator", state.locationId)
        assertFalse(state.flags["elevator_down"] == true)
        assertFalse(engine.availableActions(state).any { it.id == "lobby_north_shut" })
    }

    @Test
    fun thePumpCanBeDisconnectedTwiceAndStillFires() {
        val lobby = at("white_house_lobby").copy(inventory = listOf("bike_pump", "cricket_ball"))
        var state = go(lobby, "plug_pump", "unplug_pump", "plug_pump_again", "unplug_pump", "plug_pump_again")
        assertEquals(2, state.score)
        assertTrue(state.flags["pump_plugged"] == true)
        assertFalse("bike_pump" in state.inventory)
        state = go(state, "pump_air", "load_ball", "pull_lever_statue")
        assertTrue(state.flags["ball_on_lobby"] == true)
    }

    @Test
    fun removingTheRatFirstLetsTheNextShotOpenTheDoor() {
        val ready = at("white_house_lobby").copy(
            inventory = listOf("cricket_ball"),
            flags = at("white_house_lobby").flags + mapOf(
                "pump_plugged" to true,
                "pump_pumped" to true,
                "statue_turned" to true,
                "pipe_fixed" to true,
                "rat_removed" to false,
            ),
        )
        var state = go(ready, "take_rat", "load_ball", "pull_lever_solve")
        assertTrue(state.flags["babel_solved"] == true)
        assertEquals(at("white_house_lobby").score + 5, state.score)
    }

    @Test
    fun theDoorStaysShutWithoutPowerOrTheCode() {
        val dark = at("white_house_porch")
        assertFalse(engine.availableActions(dark).any { it.id == "enter_code" })
        val lit = dark.copy(flags = dark.flags + ("generators_on" to true))
        assertFalse(engine.availableActions(lit).any { it.id == "enter_code" })
        assertTrue(engine.availableActions(lit).any { it.id == "porch_lit" })
    }

    @Test
    fun aFullHandCannotTakeTheBall() {
        val room = at("yellow_room").copy(inventory = listOf("matchbook", "fishing_rod", "flute"))
        val refused = engine.resolve(room, "take_ball") as IslandResolution.Rejected
        assertSame(room, refused.state)
        assertEquals(IslandRejection.INVENTORY_FULL, refused.reason)
    }

    private fun at(place: String): IslandState {
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = (engine.continuePrologue(state) as IslandResolution.Accepted).state
        }
        return state.copy(
            locationId = place,
            visitedLocationIds = state.visitedLocationIds + place,
            flags = state.flags + ("reached_plateau" to true),
            introducedActIds = state.introducedActIds + "act_1" + "act_2",
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
