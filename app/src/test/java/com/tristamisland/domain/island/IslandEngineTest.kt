package com.tristamisland.domain.island

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandEngineTest {
    private val story: IslandStory = loadIsland(
        IslandEngineTest::class.java.getResource("/island/two_beaches.json")!!.readText(),
    )
    private val engine = IslandEngine(story)

    @Test
    fun newGameStartsOnTheBeachWithoutACharacterOrAClock() {
        val state = engine.newGame()
        assertEquals("immaculate_beach", state.locationId)
        assertEquals(emptyList<String>(), state.inventory)
        assertEquals(setOf("immaculate_beach"), state.visitedLocationIds)
        assertTrue(state.flags.isEmpty())
        assertTrue(state.journalIds.isEmpty())
    }

    @Test
    fun walkingWestAndBackIsRepeatableAndDeterministic() {
        val start = engine.newGame()
        val first = engine.resolve(start, "go_west") as IslandResolution.Accepted
        val second = engine.resolve(start, "go_west") as IslandResolution.Accepted
        assertEquals(first, second)
        assertEquals("beach_by_forest", first.state.locationId)
        assertEquals(setOf("immaculate_beach", "beach_by_forest"), first.state.visitedLocationIds)
        val back = engine.resolve(first.state, "go_east") as IslandResolution.Accepted
        assertEquals("immaculate_beach", back.state.locationId)
        val again = engine.resolve(back.state, "go_west") as IslandResolution.Accepted
        assertEquals("beach_by_forest", again.state.locationId)
    }

    @Test
    fun takingTheFlowerWritesTheJournalAndASecondTakeChangesNothing() {
        val beach = (engine.resolve(engine.newGame(), "go_west") as IslandResolution.Accepted).state
        val taken = engine.resolve(beach, "take_flower") as IslandResolution.Accepted
        assertEquals(listOf("flower"), taken.state.inventory)
        assertEquals(setOf("found_flower"), taken.state.journalIds)
        assertEquals("Coges una flor especialmente brillante.", taken.response)
        val again = engine.resolve(taken.state, "take_flower")
        assertTrue(again is IslandResolution.Rejected)
        assertSame(taken.state, again.state)
        assertEquals(IslandRejection.ALREADY_RESOLVED, (again as IslandResolution.Rejected).reason)
    }

    @Test
    fun aRejectedMoveDoesNotChangeTheBeach() {
        val start = engine.newGame()
        val rejected = engine.resolve(start, "go_east")
        assertSame(start, rejected.state)
        assertEquals(IslandRejection.WRONG_LOCATION, (rejected as IslandResolution.Rejected).reason)
    }

    @Test
    fun fullHandsRefuseAnotherItem() {
        val full = IslandEngine(
            story.copy(
                items = story.items + listOf(
                    IslandItem("pebble", "guijarro", "Un guijarro liso."),
                    IslandItem("shell", "concha", "Una concha vacía."),
                    IslandItem("stick", "palo", "Un palo corto."),
                ),
                initialItemIds = listOf("pebble", "shell", "stick"),
            ),
        )
        val atBeach = full.newGame().copy(
            locationId = "beach_by_forest",
            visitedLocationIds = setOf("immaculate_beach", "beach_by_forest"),
        )
        val refused = full.resolve(atBeach, "take_flower")
        assertSame(atBeach, refused.state)
        assertEquals(IslandRejection.INVENTORY_FULL, (refused as IslandResolution.Rejected).reason)
    }

    @Test
    fun aMissingDestinationDoesNotLoad() {
        val broken = story.copy(
            actions = listOf(
                story.actions.first().copy(
                    effects = listOf(IslandEffect.MoveTo("nowhere")),
                ),
            ),
        )
        val errors = validateIsland(broken)
        assertTrue(errors.any { it.contains("nowhere") })
    }

    @Test
    fun theBeachOnlyOffersTheWalkUntilYouArrive() {
        val labels = engine.availableActions(engine.newGame()).map { it.label }
        assertEquals(listOf("Ir al oeste, hacia el bosque"), labels)
        val beach = (engine.resolve(engine.newGame(), "go_west") as IslandResolution.Accepted).state
        assertEquals(
            listOf("Volver al este", "Coger una flor"),
            engine.availableActions(beach).map { it.label },
        )
        assertFalse(engine.availableActions(beach).any { it.id == "go_west" })
    }
}
