package com.tristamisland.domain.island

import com.tristamisland.data.IslandRecord
import com.tristamisland.presentation.IslandSnapshot
import com.tristamisland.presentation.presentIsland
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoastExitTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun northernSunDriesTheWoodInTwoTurns() {
        val woods = played().copy(locationId = "edge_of_woods", inventory = listOf("firewood"))
        assertFalse(engine.availableActions(woods).any { it.id == "drop_wood_damp" })
        var state = played()
        state = state.copy(
            locationId = "northern_cliffs",
            visitedLocationIds = state.visitedLocationIds + "northern_cliffs",
            inventory = listOf("matchbook", "firewood"),
            woodDampness = 5,
        )
        state = go(state, "drop_wood_damp")
        assertEquals(5, state.woodDampness)
        state = go(state, "examine_boat")
        assertEquals(3, state.woodDampness)
        state = go(state, "examine_boat")
        assertEquals(1, state.woodDampness)
        state = go(state, "arrange_fire")
        assertTrue(state.fireReady)
    }

    @Test
    fun aCookedMealAndASleepOpenThePlateau() {
        var state = played(
            "beach_west",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_north",
            "cliffs_northeast",
            "open_compartment",
            "take_rod",
            "north_southwest",
            "north_south",
            "strait_southeast",
            "cliffs_east",
            "forest_beach_north",
            "take_flower",
            "bait_with_flower",
            "take_worm",
            "bait_with_worm",
            "edge_south",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_water",
            "catch_fish",
            "sail_east",
            "give_fish",
            "drop_flute",
            "strait_southeast",
            "cliffs_east",
            "forest_beach_north",
            "take_flower",
            "bait_with_flower_again",
            "take_worm",
            "bait_with_worm_again",
            "edge_south",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_water",
            "catch_fish_again",
            "sail_east",
            "drop_rod",
            "strait_southeast",
            "cliffs_east",
            "forest_beach_north",
            "cut_branches",
            "edge_south",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_north",
            "cliffs_northeast",
            "drop_wood_damp",
            "examine_boat",
            "examine_boat",
            "arrange_fire",
            "take_knife",
            "cook_fish",
        )
        assertEquals(17, state.score)
        assertTrue(state.flags.getValue("ate_fish"))
        assertFalse(state.inventory.contains("fish"))
        assertFalse(state.inventory.contains("matchbook"))
        state = go(state, "take_net")
        listOf(
            "north_southwest",
            "north_south",
            "strait_southeast",
            "cliffs_east",
            "forest_beach_north",
            "tie_hammock",
            "sleep_hammock",
            "edge_north",
            "scrape_stairs",
        ).forEach { state = go(state, it) }
        assertEquals("top_of_stairs", state.locationId)
        assertEquals(27, state.score)
        assertTrue(state.flags.getValue("slept"))
        val play = presentIsland(story, engine, IslandSnapshot(record = IslandRecord(state, revision = 1)))
        assertEquals("Acto 2", play.actCard)
        assertEquals("Alto de la escalera", play.placeName)
    }

    private fun played(vararg actions: String): IslandState {
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = (engine.continuePrologue(state) as IslandResolution.Accepted).state
        }
        actions.forEach { state = go(state, it) }
        return state
    }

    private fun go(state: IslandState, actionId: String): IslandState {
        val resolution = engine.resolve(state, actionId)
        return (resolution as? IslandResolution.Accepted)?.state
            ?: error("$actionId rejected: ${(resolution as IslandResolution.Rejected).reason}")
    }
}
