package com.tristamisland.domain.island

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopAndTowerTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun theRockOpensBothShopsTheTowerAndTheCrudeDoor() {
        var state = go(at("scenic_view"), "take_rock")
        assertTrue("large_rock" in state.inventory)
        val shopBefore = state.score
        state = go(state, "scenic_south", "smash_shop")
        assertEquals(shopBefore + 2, state.score)
        assertFalse("large_rock" in state.inventory)
        assertFalse(engine.availableActions(state).any { it.id == "shop_in" })
        state = go(state, "enter_shop", "take_binoculars", "take_rock_shop", "leave_shop")
        assertTrue("binoculars" in state.inventory)
        val woodBefore = state.score
        state = go(state, "shop_east", "smash_woodshop", "enter_woodshop", "take_rungs")
        assertEquals(woodBefore + 2, state.score)
        val ladderBefore = state.score
        state = go(
            state,
            "leave_woodshop",
            "wood_west",
            "shop_south",
            "plaza_west",
            "pub_southwest",
            "landlord_southwest",
            "fix_ladder",
            "climb_tower",
            "take_screwdriver",
            "look_through_binoculars",
        )
        assertEquals(ladderBefore + 10, state.score)
        assertTrue(state.journalIds.contains("tristam_charred"))
        assertFalse("ladder_rungs" in state.inventory)
        state = go(state, "climb_down_tower", "tower_northeast", "landlord_east", "unscrew_door")
        assertEquals(ladderBefore + 12, state.score)
        state = go(state, "enter_crude_house", "read_code_book")
        assertTrue(state.journalIds.contains("house_code"))
        assertFalse(engine.availableActions(state).any { it.id == "read_code_book" })
    }

    @Test
    fun counterGoodsCanBePutDownAndTheCounterDoesNotReplaceThem() {
        val inside = go(at("scenic_view"), "take_rock", "scenic_south", "smash_shop", "enter_shop")
        var state = go(inside, "take_binoculars", "take_lighter")
        val refused = engine.resolve(state, "take_rock_shop") as IslandResolution.Rejected
        assertSame(state, refused.state)
        assertEquals(IslandRejection.INVENTORY_FULL, refused.reason)
        state = go(state, "drop_binoculars", "drop_lighter", "take_gum", "drop_gum", "take_sixpence", "drop_sixpence")
        assertFalse(engine.availableActions(state).any { it.id == "take_binoculars" })
        assertFalse(engine.availableActions(state).any { it.id == "take_lighter" })
        assertFalse(engine.availableActions(state).any { it.id == "take_gum" })
        assertFalse(engine.availableActions(state).any { it.id == "take_sixpence" })
        state = go(state, "take_rock_shop", "take_dropped_binoculars")
        assertEquals(listOf("matchbook", "large_rock", "binoculars"), state.inventory)
        assertFalse(state.placed[state.locationId].orEmpty().contains("binoculars"))
    }

    @Test
    fun torchAndKeyStillReachTheCharredIslandAndTheDoor() {
        val start = at("scenic_view").copy(inventory = listOf("torch", "pub_key"))
        var state = go(
            start,
            "take_rock",
            "scenic_south",
            "smash_shop",
            "enter_shop",
            "take_binoculars",
            "drop_binoculars",
            "take_rock_shop",
            "leave_shop",
            "shop_east",
            "smash_woodshop",
            "enter_woodshop",
            "take_rungs",
            "drop_rungs",
            "take_saw",
            "drop_saw",
        )
        assertFalse(engine.availableActions(state).any { it.id == "take_rungs" })
        assertFalse(engine.availableActions(state).any { it.id == "take_saw" })
        state = go(
            state,
            "take_dropped_rungs",
            "leave_woodshop",
            "wood_west",
            "shop_south",
            "plaza_west",
            "pub_southwest",
            "landlord_southwest",
            "fix_ladder",
            "tower_northeast",
            "landlord_northeast",
            "pub_east",
            "plaza_north",
            "enter_shop",
            "take_dropped_binoculars",
            "leave_shop",
            "shop_south",
            "plaza_west",
            "pub_southwest",
            "landlord_southwest",
            "climb_tower",
            "look_through_binoculars",
            "drop_binoculars",
            "take_screwdriver",
        )
        assertTrue(state.journalIds.contains("tristam_charred"))
        assertFalse("binoculars" in state.inventory)
        assertTrue("screwdriver" in state.inventory)
        state = go(
            state,
            "drop_screwdriver",
            "take_dropped_binoculars",
            "drop_binoculars",
            "take_dropped_screwdriver",
            "climb_down_tower",
            "tower_northeast",
            "landlord_east",
            "unscrew_door",
        )
        assertTrue(state.flags["crude_door_open"] == true)
        assertFalse(engine.availableActions(state).any { it.id == "take_screwdriver" })
    }

    @Test
    fun aFullHandCannotTakeTheBinoculars() {
        val inside = go(at("scenic_view"), "take_rock", "scenic_south", "smash_shop", "enter_shop")
        val full = inside.copy(inventory = listOf("matchbook", "fishing_rod", "flute"))
        val refused = engine.resolve(full, "take_binoculars") as IslandResolution.Rejected
        assertSame(full, refused.state)
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
