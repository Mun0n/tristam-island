package com.tristamisland.domain.island

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class KitchenPowerTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun kitchenOilStartsTheGeneratorsAndLightsTheKeypad() {
        var state = at("inside_pub").copy(inventory = listOf("matchbook", "fishing_knife"))
        state = go(state, "enter_kitchen", "grab_oil")
        assertFalse("cooking_oil" in state.inventory)
        state = go(state, "cut_oil")
        assertEquals(listOf("matchbook", "cooking_oil"), state.inventory)
        assertEquals(listOf("fishing_knife"), state.placed["pub_kitchen"])
        assertFalse(engine.availableActions(state).any { it.id == "cut_oil" })
        state = go(state, "take_pot", "put_oil_in_pot", "take_ground_knife")
        assertFalse("cooking_oil" in state.inventory)
        assertTrue(state.flags["oil_in_pot"] == true)
        assertTrue("fishing_knife" in state.inventory)
        state = go(state, "drop_pot", "leave_kitchen")
        assertFalse(engine.availableActions(state).any { it.id == "take_dropped_pot" })
        state = go(state, "enter_kitchen", "take_dropped_pot")
        val solid = state.copy(locationId = "generators", visitedLocationIds = state.visitedLocationIds + "generators")
        val refusedPour = go(solid, "pour_solid")
        assertFalse(refusedPour.flags["tank_full"] == true)
        state = state.copy(locationId = "scenic_view", visitedLocationIds = state.visitedLocationIds + "scenic_view")
        state = go(state, "light_scenic_no_lighter")
        assertFalse(state.flags["scenic_fire"] == true)
        state = go(state.copy(inventory = listOf("pot", "lighter")), "light_scenic_fire", "melt_oil")
        assertTrue(state.flags["oil_melted"] == true)
        assertFalse(state.flags["oil_in_pot"] == true)
        assertTrue(placePassage(story, state).contains("arde una hoguera"))
        state = state.copy(locationId = "generators")
        val before = state.score
        state = go(state, "crank_dry")
        assertEquals(before, state.score)
        state = go(state, "pour_oil", "crank_generators")
        assertEquals(before + 5, state.score)
        assertTrue(state.journalIds.contains("generators_running"))
        assertTrue(placePassage(story, state).contains("están en marcha"))
        val again = engine.resolve(state, "crank_generators") as IslandResolution.Rejected
        assertSame(state, again.state)
        assertEquals(IslandRejection.ALREADY_RESOLVED, again.reason)
        val porch = state.copy(
            locationId = "white_house_porch",
            visitedLocationIds = state.visitedLocationIds + "white_house_porch",
        )
        assertTrue(placePassage(story, porch).contains("El teclado tiene luz."))
        assertFalse(placePassage(story, porch).contains("Sin luz"))
        assertTrue(engine.availableActions(porch).any { it.id == "porch_lit" })
        assertFalse(engine.availableActions(porch).any { it.id == "porch_in" })
        val pub = porch.copy(locationId = "inside_pub")
        assertTrue(placePassage(story, pub).contains("gramola antigua, encendida."))
    }

    @Test
    fun aFullHandCannotTakeThePot() {
        val kitchen = at("pub_kitchen").copy(inventory = listOf("matchbook", "fishing_rod", "flute"))
        val refused = engine.resolve(kitchen, "take_pot") as IslandResolution.Rejected
        assertSame(kitchen, refused.state)
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
            introducedActIds = state.introducedActIds + "act_1",
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
