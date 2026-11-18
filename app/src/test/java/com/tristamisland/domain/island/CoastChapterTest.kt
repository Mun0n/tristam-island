package com.tristamisland.domain.island

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CoastChapterTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun theCoastActNamesTheLowerIsland() {
        assertEquals("lower_island", story.acts.first { it.id == "act_1" }.musicRegion)
        assertEquals("plateau", story.acts.first { it.id == "act_2" }.musicRegion)
        assertEquals("mansion", story.acts.first { it.id == "act_3" }.musicRegion)
        assertEquals("laboratory", story.acts.first { it.id == "act_4" }.musicRegion)
        assertEquals(listOf("act_1", "act_2", "act_3", "act_4"), story.acts.map { it.id })
        assertTrue(story.locations.all { it.actId in story.acts.map { act -> act.id } })
        val broken = story.copy(
            acts = story.acts.map { act ->
                if (act.id == "act_1") act.copy(musicRegion = "Lower Island") else act
            },
        )
        assertTrue(validateIsland(broken).any { it.contains("music region") })
    }

    @Test
    fun prologueBlocksTheBeachUntilBothParagraphsAreRead() {
        var state = engine.newGame()
        assertEquals(listOf("matchbook"), state.inventory)
        assertTrue(engine.availableActions(state).isEmpty())
        state = accepted(engine.continuePrologue(state)).state
        assertTrue(engine.inPrologue(state))
        state = accepted(engine.continuePrologue(state)).state
        assertFalse(engine.inPrologue(state))
        assertEquals("immaculate_beach", state.locationId)
        assertTrue(engine.availableActions(state).any { it.id == "beach_west" })
    }

    @Test
    fun flowerIsTakenOnceAndTheStonesScoreOnePoint() {
        var state = finishPrologue()
        state = go(state, "beach_west")
        state = go(state, "forest_beach_north")
        val taken = accepted(engine.resolve(state, "take_flower"))
        assertEquals(listOf("matchbook", "flower"), taken.state.inventory)
        val refused = engine.resolve(taken.state, "take_flower")
        assertSame(taken.state, (refused as IslandResolution.Rejected).state)

        var woods = go(taken.state, "edge_north")
        woods = accepted(engine.resolve(woods, "study_stones")).state
        assertEquals(1, woods.score)
        assertTrue(woods.journalIds.contains("white_stones"))
        val climb = accepted(engine.resolve(woods, "climb_hill"))
        assertEquals("deep_woods", climb.state.locationId)
        assertEquals(1, climb.state.score)
        assertTrue(climb.state.flags.getValue("stones_known"))
    }

    @Test
    fun theNorthShoreReachesTheBoatAndClosesOnTheCove() {
        var state = finishPrologue()
        listOf(
            "beach_west",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_north",
            "cliffs_northeast",
        ).forEach { id ->
            state = go(state, id)
        }
        assertEquals("northern_cliffs", state.locationId)
        assertEquals(0, state.score)
        state = accepted(engine.resolve(state, "examine_boat")).state
        assertEquals(0, state.score)
        assertTrue(state.journalIds.contains("north_boat"))
        state = go(state, "north_east")
        assertEquals("northeastern_tip", state.locationId)
        val blocked = accepted(engine.resolve(state, "tip_southwest"))
        assertEquals("northeastern_tip", blocked.state.locationId)
        state = go(blocked.state, "tip_southeast")
        assertEquals("cove", state.locationId)
        state = go(state, "cove_northwest")
        assertEquals("northeastern_tip", state.locationId)
    }

    @Test
    fun theOceanIsOnePlaceAndTheSandbankStopsPastNine() {
        var state = finishPrologue()
        state = go(state, "beach_water")
        assertEquals("ocean", state.locationId)
        assertEquals("immaculate_beach", state.shoreId)
        assertEquals(0, state.seaHeading)
        assertEquals("En el océano", engine.placeName(state))
        assertFalse(engine.availableActions(state).any { it.id == "sail_east" })

        state = go(state, "sail_west")
        assertEquals("beach_by_forest", state.shoreId)
        assertEquals(0, state.seaHeading)
        state = go(state, "sail_west")
        assertEquals("southwestern_cliffs", state.shoreId)
        assertEquals(1, state.seaHeading)
        assertEquals("Agua hasta la rodilla", engine.placeName(state))

        repeat(10) { state = go(state, "sail_southwest") }
        assertEquals(10, state.sandbankDepth)
        assertTrue(engine.passage(state).contains("horizonte"))
        val tooDeep = accepted(engine.resolve(state, "sail_southwest"))
        assertEquals(10, tooDeep.state.sandbankDepth)
        assertEquals(
            "Intentas seguir, pero el fondo cae y nadas de vuelta hasta donde haces pie.",
            tooDeep.response,
        )
        state = go(tooDeep.state, "sail_northeast")
        assertEquals(9, state.sandbankDepth)
        repeat(9) { state = go(state, "sail_northeast") }
        assertEquals(0, state.sandbankDepth)
        state = go(state, "sail_northeast")
        assertEquals("southwestern_cliffs", state.locationId)
        assertEquals(null, state.seaHeading)
    }

    @Test
    fun theFirstFishFeedsTheAlbatrossAndTheTowerScoresOnce() {
        var state = finishPrologue()
        listOf(
            "beach_west",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_north",
            "cliffs_northeast",
        ).forEach { state = go(state, it) }
        state = go(state, "open_compartment")
        val opened = state
        val carryingRod = go(opened, "take_rod")
        val carryingNet = go(carryingRod, "take_net")
        val full = engine.resolve(carryingNet, "take_knife")
        assertEquals(IslandRejection.INVENTORY_FULL, (full as IslandResolution.Rejected).reason)
        assertFalse(carryingNet.flags.getValue("knife_taken"))
        val stowed = go(carryingNet, "leave_net")
        assertFalse(stowed.inventory.contains("fishing_net"))
        assertFalse(stowed.flags.getValue("net_taken"))
        val retaken = go(stowed, "take_net")
        assertTrue(retaken.inventory.contains("fishing_net"))
        state = carryingRod

        state = go(state, "north_southwest")
        state = go(state, "north_south")
        state = go(state, "strait_southeast")
        state = go(state, "cliffs_east")
        state = go(state, "forest_beach_north")
        state = go(state, "take_flower")
        state = go(state, "bait_with_flower")
        assertEquals(1, state.score)
        state = go(state, "take_worm")
        state = go(state, "bait_with_worm")
        assertEquals(2, state.score)
        state = go(state, "edge_south")
        state = go(state, "forest_beach_west")
        state = go(state, "cliffs_northwest")
        state = go(state, "strait_water")
        assertEquals(2, state.seaHeading)
        assertTrue(engine.passage(state).contains("pescar"))
        state = go(state, "catch_fish")
        assertEquals(7, state.score)
        assertTrue(state.inventory.contains("fish"))
        assertFalse(engine.availableActions(state).any { it.id == "give_fish" })
        state = go(state, "sail_east")
        assertEquals("narrow_strait", state.locationId)
        state = go(state, "give_fish")
        assertTrue(state.inventory.contains("flute"))
        assertFalse(state.inventory.contains("fish"))
        assertEquals(7, state.score)

        state = go(state, "strait_southeast")
        state = go(state, "cliffs_water")
        state = go(state, "sail_southwest")
        state = go(state, "examine_tower")
        assertEquals(8, state.score)
        val again = engine.resolve(state, "examine_tower")
        assertSame(state, (again as IslandResolution.Rejected).state)
    }

    @Test
    fun aSaveMayOmitNewFlagsAndMayNotInventOne() {
        val played = finishPrologue()
        val older = played.copy(flags = mapOf("stones_known" to false))
        assertTrue(engine.canResume(older))
        val foreign = played.copy(flags = played.flags + ("saw_north_coast" to true))
        assertFalse(engine.canResume(foreign))
    }

    @Test
    fun aNewGameShowsTheFirstActUntilItIsAcknowledged() {
        val opening = engine.newGame()
        assertTrue(opening.introducedActIds.isEmpty())
        val introduced = accepted(engine.acknowledgeAct(opening))
        assertEquals(setOf("act_1"), introduced.state.introducedActIds)
        assertEquals(opening.locationId, introduced.state.locationId)
        assertEquals(opening.score, introduced.state.score)
        val again = engine.acknowledgeAct(introduced.state)
        assertSame(introduced.state, (again as IslandResolution.Rejected).state)
    }

    private fun finishPrologue(): IslandState {
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = accepted(engine.continuePrologue(state)).state
        }
        return state
    }

    private fun go(state: IslandState, actionId: String): IslandState =
        accepted(engine.resolve(state, actionId)).state

    private fun accepted(resolution: IslandResolution): IslandResolution.Accepted =
        resolution as IslandResolution.Accepted
}
