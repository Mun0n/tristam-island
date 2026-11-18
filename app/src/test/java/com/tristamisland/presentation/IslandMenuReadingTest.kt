package com.tristamisland.presentation

import com.tristamisland.data.IslandRecord
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.loadIsland
import java.io.File
import com.tristamisland.domain.island.IslandResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandMenuReadingTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun aSavedPrologueNamesTheActAndTheBeach() {
        val play = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(engine.newGame(), revision = 1)),
        )
        assertEquals("Costa baja", play.actTitle)
        assertEquals("Playa inmaculada", play.placeName)
        assertTrue(play.inPrologue)
    }

    @Test
    fun theJournalKeepsAuthoredOrderAndPlace() {
        val state = engine.newGame().copy(journalIds = setOf("north_boat", "found_flower"))
        val play = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(state, revision = 1)),
        )
        assertEquals(
            listOf(
                IslandNote(
                    id = "found_flower",
                    place = "Linde del bosque",
                    text = "Cogiste una flor brillante. Huele poco, pero el color no se olvida.",
                ),
                IslandNote(
                    id = "north_boat",
                    place = "Acantilados del norte",
                    text = "Un bote de pesca está amarrado a un muelle, bajo el acantilado de tiza. El casco está rajado.",
                ),
            ),
            play.journal,
        )
    }

    @Test
    fun theLowerIslandKeepsOneMusicRegionFromThePrologueThroughAMove() {
        assertEquals("lower_island", story.acts.first { it.id == "act_1" }.musicRegion)
        val opening = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(engine.newGame(), revision = 1)),
        )
        assertTrue(opening.inPrologue)
        assertEquals("lower_island", opening.musicRegion)
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = (engine.continuePrologue(state) as IslandResolution.Accepted).state
        }
        state = (engine.resolve(state, "beach_west") as IslandResolution.Accepted).state
        val moved = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(state, revision = 2)),
        )
        assertEquals("beach_by_forest", moved.locationId)
        assertEquals("lower_island", moved.musicRegion)
    }

    @Test
    fun theActCardUsesTheActOrderAndDisappearsOnceIntroduced() {
        val opening = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(engine.newGame(), revision = 1)),
        )
        assertEquals("Acto 1", opening.actCard)
        val seen = engine.newGame().copy(introducedActIds = setOf("act_1"))
        val beach = presentIsland(story, engine, IslandSnapshot(record = IslandRecord(seen, revision = 1)))
        assertNull(beach.actCard)

        val twoActs = loadIsland(
            """
            {
              "schemaVersion": 2,
              "id": "two_acts",
              "title": "Dos actos",
              "startLocationId": "shore",
              "acts": [
                { "id": "act_1", "title": "Costa", "musicRegion": "lower_island" },
                { "id": "act_2", "title": "Poblado", "musicRegion": "upper_island" }
              ],
              "locations": [
                { "id": "shore", "name": "Costa", "description": "La costa.", "actId": "act_1" },
                { "id": "plaza", "name": "Plaza", "description": "La plaza.", "actId": "act_2" }
              ]
            }
            """.trimIndent(),
        )
        val later = IslandEngine(twoActs).newGame().copy(
            locationId = "plaza",
            visitedLocationIds = setOf("shore", "plaza"),
        )
        val card = presentIsland(twoActs, IslandEngine(twoActs), IslandSnapshot(record = IslandRecord(later, revision = 1)))
        assertEquals("Acto 2", card.actCard)
    }

    @Test
    fun anEmptyMenuHasNoPlace() {
        val play = presentIsland(story, engine, IslandSnapshot())
        assertEquals("", play.actTitle)
        assertEquals("", play.placeName)
    }
}
