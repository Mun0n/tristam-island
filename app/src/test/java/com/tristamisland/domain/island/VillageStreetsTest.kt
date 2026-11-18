package com.tristamisland.domain.island

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class VillageStreetsTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun bothArrivalsMeetInThePlazaAndTheFountainScoresOnce() {
        var state = arrived("outside_storehouse")
        state = go(state, "storehouse_south", "garden_east", "pub_east")
        assertEquals("central_plaza", state.locationId)
        val before = state.score
        state = go(state, "drink_fountain")
        assertEquals(before + 1, state.score)
        assertTrue(state.flags.getValue("fountain_drank"))
        val again = engine.resolve(state, "drink_fountain_again") as IslandResolution.Accepted
        assertEquals(state.score, again.state.score)
        assertEquals("central_plaza", again.state.locationId)

        state = go(again.state, "plaza_south", "houses_south")
        assertEquals("top_of_stairs", state.locationId)
        val locked = engine.resolve(go(arrived("outside_pub"), "pub_in"), "pub_in")
        assertEquals("outside_pub", (locked as IslandResolution.Accepted).state.locationId)
    }

    @Test
    fun theStorehouseTorchNeedsAFreeHand() {
        var state = arrived("storehouse_front")
        val full = state.copy(inventory = listOf("matchbook", "fishing_rod", "flute"))
        val refused = engine.resolve(full, "look_shelves")
        assertSame(full, (refused as IslandResolution.Rejected).state)
        state = go(state, "look_shelves")
        assertTrue("torch" in state.inventory)
        assertEquals(true, state.flags["torch_found"])
        state = go(state, "light_torch")
        assertEquals(true, state.flags["torch_on"])
        state = go(state, "douse_torch")
        assertEquals(false, state.flags["torch_on"])
    }

    @Test
    fun theSafeKeyOpensThePubAndTheMapNamesTheIsland() {
        var state = go(arrived("pub_landlord_house"), "landlord_in")
        val full = state.copy(inventory = listOf("matchbook", "fishing_rod", "flute"))
        val refused = engine.resolve(full, "open_safe") as IslandResolution.Rejected
        assertSame(full, refused.state)
        assertEquals(IslandRejection.INVENTORY_FULL, refused.reason)
        state = go(state, "open_safe")
        assertTrue("pub_key" in state.inventory)
        val before = state.score
        val lockedDoor = go(state, "landlord_out", "landlord_northeast")
        assertTrue(placePassage(story, lockedDoor).contains("La puerta del pub está cerrada."))
        state = go(lockedDoor, "unlock_pub")
        assertTrue(placePassage(story, state).contains("La puerta del pub está abierta."))
        assertFalse(placePassage(story, state).contains("cerrada"))
        state = go(state, "enter_pub", "read_island_map")
        assertEquals("inside_pub", state.locationId)
        assertEquals(before + 2, state.score)
        assertTrue(state.journalIds.contains("kingsley_island"))
        assertFalse(engine.availableActions(state).any { it.id == "read_island_map" })
        state = go(state, "read_notice")
        assertTrue(state.journalIds.contains("garden_shifts"))
    }

    @Test
    fun theHoleAndTheTreeEachScoreOnceAndBothCanLeave() {
        var byHole = arrived("generators")
        byHole = go(byHole, "look_bushes", "look_bushes_again")
        assertEquals(true, byHole.flags["hole_found"])
        val before = byHole.score
        byHole = go(byHole, "crawl_hole")
        assertEquals("field", byHole.locationId)
        assertEquals(before + 5, byHole.score)
        byHole = go(byHole, "field_east")
        assertEquals("white_house_porch", byHole.locationId)
        val door = engine.resolve(byHole, "porch_in") as IslandResolution.Accepted
        assertEquals("white_house_porch", door.state.locationId)
        byHole = go(door.state, "porch_west", "crawl_out")
        assertEquals("generators", byHole.locationId)

        var byTree = arrived("around_fence")
        val treeBefore = byTree.score
        byTree = go(byTree, "climb_tree")
        assertEquals("field", byTree.locationId)
        assertEquals(treeBefore + 5, byTree.score)
        byTree = go(byTree, "look_fence", "crawl_out", "crawl_hole_later")
        assertEquals("field", byTree.locationId)
        assertEquals(treeBefore + 5, byTree.score)
        assertFalse(engine.availableActions(arrived("base_of_tower")).any { it.id == "climb_tree" })
    }

    private fun arrived(place: String): IslandState {
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
            current = (engine.resolve(current, id) as IslandResolution.Accepted).state
        }
        return current
    }
}
