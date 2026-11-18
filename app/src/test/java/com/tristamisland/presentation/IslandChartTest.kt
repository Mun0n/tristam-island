package com.tristamisland.presentation

import com.tristamisland.data.IslandRecord
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.IslandResolution
import com.tristamisland.domain.island.loadIsland
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandChartTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun aNewGameShowsOnlyTheSouthBeach() {
        val chart = present().chart
        assertEquals(listOf("immaculate_beach"), chart.marks.map { it.id })
        assertTrue(chart.marks.single().here)
        assertTrue(chart.links.isEmpty())
        assertTrue(chart.elsewhere.isEmpty())
    }

    @Test
    fun walkingWestDrawsTheNextShoreAndHidesTheDock() {
        val chart = present(played("beach_west")).chart
        assertEquals(setOf("immaculate_beach", "beach_by_forest"), chart.marks.map { it.id }.toSet())
        assertEquals("beach_by_forest", chart.marks.single { it.here }.id)
        assertEquals(1, chart.links.size)
        assertFalse(chart.marks.any { it.id == "northern_cliffs" })
    }

    @Test
    fun theOceanSitsOffshoreOfTheShoreYouFace() {
        val south = present(played("beach_water")).chart
        val beach = south.marks.single { it.id == "immaculate_beach" }
        val sea = south.marks.single { it.id == "ocean" }
        assertTrue(sea.here)
        assertFalse(beach.here)
        assertTrue(sea.y > beach.y)

        val north = played(
            "beach_west",
            "forest_beach_west",
            "cliffs_northwest",
            "strait_north",
            "cliffs_northeast",
            "north_water",
        )
        val chart = present(north).chart
        val dock = chart.marks.single { it.id == "northern_cliffs" }
        val water = chart.marks.single { it.id == "ocean" }
        assertTrue(water.y < dock.y)
        val village = coastChart(
            visited = setOf("northern_cliffs", "top_of_stairs", "central_plaza", "houses"),
            here = "ocean",
            shoreId = "northern_cliffs",
            seaInland = 4,
            names = emptyMap(),
        )
        val offshore = village.marks.single { it.id == "ocean" }
        val belowTheStreets = village.marks.filter { it.id != "ocean" && it.id != "northern_cliffs" }.maxOf { it.y }
        assertTrue(offshore.y > belowTheStreets)
        assertTrue(offshore.y < village.marks.single { it.id == "northern_cliffs" }.y)
        assertNull(chart.elsewhere.singleOrNull { it.name == "En el océano" })
    }

    @Test
    fun aKnownPlaceCanBeOpenedFromTheMapAndTheSeaOnlyFromAShore() {
        val walked = played("beach_west", "forest_beach_water")
        val back = engine.travelTo(walked, "immaculate_beach") as IslandResolution.Accepted
        assertEquals("immaculate_beach", back.state.locationId)
        assertEquals(null, back.state.seaHeading)
        assertEquals("Te diriges a Playa inmaculada.", back.response)
        val ashore = present(back.state)
        assertTrue(ashore.chart.elsewhere.single { it.id == "ocean" }.canTravel)
        val intoWater = engine.travelTo(back.state, "ocean") as IslandResolution.Accepted
        assertEquals("ocean", intoWater.state.locationId)
        assertEquals(0, intoWater.state.seaHeading)
        assertEquals("immaculate_beach", intoWater.state.shoreId)
        assertEquals("Te metes en el agua.", intoWater.response)

        val woods = engine.travelTo(back.state, "beach_by_forest") as IslandResolution.Accepted
        val inland = (engine.resolve(woods.state, "forest_beach_north") as IslandResolution.Accepted).state
        assertFalse(present(inland).chart.elsewhere.single { it.id == "ocean" }.canTravel)
        val refused = engine.travelTo(inland, "ocean")
        assertSame(inland, (refused as IslandResolution.Rejected).state)

        val unknown = engine.travelTo(inland, "northern_cliffs")
        assertSame(inland, (unknown as IslandResolution.Rejected).state)
    }

    @Test
    fun aVisitedVillageStreetIsDrawnOnTheMap() {
        val chart = coastChart(
            visited = setOf("outside_storehouse", "outside_pub"),
            here = "outside_pub",
            shoreId = null,
            seaInland = null,
            names = mapOf(
                "outside_storehouse" to "Fuera del almacén",
                "outside_pub" to "El Barco y el Ancla",
            ),
        )
        assertEquals(setOf("outside_storehouse", "outside_pub"), chart.marks.map { it.id }.toSet())
        assertEquals(1, chart.links.size)
        assertTrue(chart.elsewhere.isEmpty())
    }

    @Test
    fun theMansionReplacesTheIslandMap() {
        val chart = progressChart(
            visited = setOf("immaculate_beach", "white_house_porch", "white_house_lobby", "mansion_library"),
            here = "white_house_lobby",
            shoreId = null,
            seaInland = null,
            names = mapOf(
                "immaculate_beach" to "Playa inmaculada",
                "white_house_porch" to "Porche de la casa blanca",
                "white_house_lobby" to "Recibidor",
                "mansion_library" to "Biblioteca",
            ),
        )
        assertEquals(
            setOf("white_house_porch", "white_house_lobby", "mansion_library"),
            chart.marks.map { it.id }.toSet(),
        )
        val lobby = chart.marks.single { it.id == "white_house_lobby" }
        val library = chart.marks.single { it.id == "mansion_library" }
        val porch = chart.marks.single { it.id == "white_house_porch" }
        assertTrue(lobby.here)
        assertTrue(library.y > lobby.y)
        assertTrue(porch.x < lobby.x)
        assertTrue(kotlin.math.abs(porch.x - library.x) > 0.2f)
        assertTrue(distanceToSegment(library.x, library.y, lobby.x, lobby.y, porch.x, porch.y) > 0.15f)
        assertEquals(2, chart.links.size)
        assertEquals(listOf("Playa inmaculada"), chart.elsewhere.map { it.name })
    }

    @Test
    fun theLaboratoryReplacesTheMansionAndTheElevatorChangesFloor() {
        val upstairs = progressChart(
            visited = setOf("antechamber", "inside_elevator", "lab_lobby"),
            here = "inside_elevator",
            shoreId = null,
            seaInland = null,
            names = mapOf(
                "antechamber" to "Antesala",
                "inside_elevator" to "Ascensor",
                "lab_lobby" to "Vestíbulo del laboratorio",
            ),
            elevatorDown = false,
        )
        assertEquals(setOf("antechamber", "inside_elevator"), upstairs.marks.map { it.id }.toSet())
        assertEquals(listOf("Vestíbulo del laboratorio"), upstairs.elsewhere.map { it.name })

        val downstairs = progressChart(
            visited = setOf("white_house_lobby", "inside_elevator", "lab_lobby", "comms_room"),
            here = "lab_lobby",
            shoreId = null,
            seaInland = null,
            names = mapOf(
                "white_house_lobby" to "Recibidor",
                "inside_elevator" to "Ascensor",
                "lab_lobby" to "Vestíbulo del laboratorio",
                "comms_room" to "Sala de comunicaciones",
            ),
            elevatorDown = true,
        )
        assertEquals(setOf("inside_elevator", "lab_lobby", "comms_room"), downstairs.marks.map { it.id }.toSet())
        val lobby = downstairs.marks.single { it.id == "lab_lobby" }
        val radio = downstairs.marks.single { it.id == "comms_room" }
        assertTrue(lobby.here)
        assertTrue(radio.x < lobby.x)
        assertEquals(listOf("Recibidor"), downstairs.elsewhere.map { it.name })
    }

    @Test
    fun aPlaceWithoutAChartSpotIsNamedUnderTheMap() {
        val chart = coastChart(
            visited = setOf("plaza"),
            here = "plaza",
            shoreId = null,
            seaInland = null,
            names = mapOf("plaza" to "Plaza"),
        )
        assertTrue(chart.marks.isEmpty())
        assertEquals(listOf("Plaza"), chart.elsewhere.map { it.name })
    }

    private fun distanceToSegment(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax
        val dy = by - ay
        val length = dx * dx + dy * dy
        val t = (((px - ax) * dx + (py - ay) * dy) / length).coerceIn(0f, 1f)
        val qx = ax + t * dx
        val qy = ay + t * dy
        val ox = px - qx
        val oy = py - qy
        return kotlin.math.sqrt(ox * ox + oy * oy)
    }

    private fun present(state: com.tristamisland.domain.island.IslandState = engine.newGame()) =
        presentIsland(story, engine, IslandSnapshot(record = IslandRecord(state, revision = 1)))

    private fun played(vararg actions: String): com.tristamisland.domain.island.IslandState {
        var state = engine.newGame()
        repeat(story.prologue.size) {
            state = (engine.continuePrologue(state) as IslandResolution.Accepted).state
        }
        actions.forEach { id ->
            state = (engine.resolve(state, id) as IslandResolution.Accepted).state
        }
        return state
    }
}
