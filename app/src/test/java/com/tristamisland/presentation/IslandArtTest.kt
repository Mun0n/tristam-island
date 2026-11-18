package com.tristamisland.presentation

import com.tristamisland.R
import com.tristamisland.data.IslandRecord
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.loadIsland
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IslandArtTest {
    private val story = loadIsland(File("src/main/assets/island/coast.json").readText())
    private val engine = IslandEngine(story)

    @Test
    fun thePrologueOpensOnTheIslandAndThenTheFall() {
        assertEquals(
            R.drawable.island_prologue_island,
            islandArtRes(inPrologue = true, prologueIndex = 0, locationId = "immaculate_beach"),
        )
        assertEquals(
            R.drawable.island_prologue_fall,
            islandArtRes(inPrologue = true, prologueIndex = 1, locationId = "immaculate_beach"),
        )
    }

    @Test
    fun everyCoastPlaceHasItsOwnPicture() {
        val expected = mapOf(
            "immaculate_beach" to R.drawable.island_immaculate_beach,
            "end_of_beach" to R.drawable.island_end_of_beach,
            "cove" to R.drawable.island_cove,
            "beach_by_forest" to R.drawable.island_beach_by_forest,
            "edge_of_woods" to R.drawable.island_edge_of_woods,
            "deep_woods" to R.drawable.island_deep_woods,
            "southwestern_cliffs" to R.drawable.island_southwestern_cliffs,
            "narrow_strait" to R.drawable.island_narrow_strait,
            "northwestern_cliffs" to R.drawable.island_northwestern_cliffs,
            "northern_cliffs" to R.drawable.island_north_boat,
            "northeastern_tip" to R.drawable.island_northeastern_tip,
            "ocean" to R.drawable.island_prologue_island,
            "outside_storehouse" to R.drawable.island_prologue_island,
            "top_of_stairs" to R.drawable.island_deep_woods,
            "community_garden" to R.drawable.island_prologue_island,
            "outside_pub" to R.drawable.island_prologue_island,
            "pub_landlord_house" to R.drawable.island_prologue_island,
            "base_of_tower" to R.drawable.island_prologue_island,
            "central_plaza" to R.drawable.island_prologue_island,
            "houses" to R.drawable.island_prologue_island,
            "cliff_house" to R.drawable.island_prologue_island,
            "inside_cliff_house" to R.drawable.island_deep_woods,
            "shop" to R.drawable.island_prologue_island,
            "woodshop" to R.drawable.island_prologue_island,
            "scenic_view" to R.drawable.island_prologue_island,
            "generators" to R.drawable.island_prologue_island,
            "fence_gate" to R.drawable.island_prologue_island,
            "around_fence" to R.drawable.island_prologue_island,
            "field" to R.drawable.island_prologue_island,
            "white_house_porch" to R.drawable.island_prologue_island,
            "storehouse_front" to R.drawable.island_deep_woods,
            "inside_landlord_house" to R.drawable.island_deep_woods,
            "inside_pub" to R.drawable.island_deep_woods,
            "pub_kitchen" to R.drawable.island_deep_woods,
            "inside_shop" to R.drawable.island_deep_woods,
            "inside_woodshop" to R.drawable.island_deep_woods,
            "top_of_tower" to R.drawable.island_prologue_island,
            "inside_crude_house" to R.drawable.island_deep_woods,
            "white_house_lobby" to R.drawable.island_deep_woods,
            "mansion_library" to R.drawable.island_deep_woods,
            "cafeteria" to R.drawable.island_deep_woods,
            "mezzanine" to R.drawable.island_deep_woods,
            "quarters" to R.drawable.island_deep_woods,
            "yellow_room" to R.drawable.island_deep_woods,
            "white_room" to R.drawable.island_deep_woods,
            "antechamber" to R.drawable.island_deep_woods,
            "inside_elevator" to R.drawable.island_deep_woods,
            "lab_lobby" to R.drawable.island_deep_woods,
            "lab_hall" to R.drawable.island_deep_woods,
            "lab_bench" to R.drawable.island_deep_woods,
            "lab_think" to R.drawable.island_deep_woods,
            "lab_cross" to R.drawable.island_deep_woods,
            "conference" to R.drawable.island_deep_woods,
            "hall_window" to R.drawable.island_deep_woods,
            "storage" to R.drawable.island_deep_woods,
            "major_door" to R.drawable.island_deep_woods,
            "major_office" to R.drawable.island_deep_woods,
            "comms_room" to R.drawable.island_deep_woods,
        )
        assertEquals(expected.keys, story.locations.map { it.id }.toSet())
        expected.forEach { (place, art) ->
            assertEquals(art, islandArtRes(inPrologue = false, prologueIndex = 2, locationId = place))
        }
    }

    @Test
    fun anUnknownPlaceHasNoPicture() {
        assertNull(islandArtRes(inPrologue = false, prologueIndex = 2, locationId = "plaza"))
    }

    @Test
    fun aNewGameCarriesTheOpeningPictureFacts() {
        val play = presentIsland(
            story,
            engine,
            IslandSnapshot(record = IslandRecord(engine.newGame(), revision = 1)),
        )
        assertEquals("immaculate_beach", play.locationId)
        assertEquals(0, play.prologueIndex)
        assertEquals("Acto 1", play.actCard)
        assertNotNull(islandArtRes(play.inPrologue, play.prologueIndex, play.locationId))
    }
}
