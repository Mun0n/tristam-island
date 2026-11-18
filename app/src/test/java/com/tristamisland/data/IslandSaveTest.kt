package com.tristamisland.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.loadIsland
import com.tristamisland.presentation.IslandSession
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IslandSaveTest {
    @Test
    fun aTurnSurvivesANewSessionAndASecondGameReplacesIt() = runBlocking {
        val database = openIslandMemory()
        val store = RoomIslandGameStore(database.islandSaveDao())
        val story = loadIsland(File("src/main/assets/island/coast.json").readText())
        val engine = IslandEngine(story)
        val first = IslandSession(engine, store)
        assertNull(first.restore().record)
        var snapshot = first.newGame()
        snapshot = first.acknowledgeAct()
        assertEquals(setOf("act_1"), snapshot.record?.state?.introducedActIds)
        snapshot = first.continuePrologue()
        val resumed = IslandSession(engine, store).restore()
        assertEquals(1, resumed.record?.state?.prologueIndex)
        assertEquals(setOf("act_1"), resumed.record?.state?.introducedActIds)
        assertEquals(snapshot.record?.revision, resumed.record?.revision)

        val replaced = IslandSession(engine, store).replace()
        assertEquals(0, replaced.record?.state?.prologueIndex)
        assertEquals(0, replaced.record?.revision)
        database.close()
    }

    @Test
    fun aSaveForAMissingPlaceIsDropped() = runBlocking {
        val database = openIslandMemory()
        val store = RoomIslandGameStore(database.islandSaveDao())
        val story = loadIsland(File("src/main/assets/island/coast.json").readText())
        val engine = IslandEngine(story)
        val saved = store.commit(engine.newGame().copy(locationId = "missing_place"), expectedRevision = null)
        assertEquals("missing_place", (saved as IslandCommit.Saved).record.state.locationId)
        val resumed = IslandSession(engine, store).restore()
        assertNull(resumed.record)
        assertNull(store.load())
        database.close()
    }

    @Test
    fun anOlderSaveWithoutTheActCardStillOpens() {
        val story = loadIsland(File("src/main/assets/island/coast.json").readText())
        val encoded = encodeIslandState(IslandEngine(story).newGame())
        val older = encoded
            .replace(Regex(""","introducedActIds":\\[[^]]*\\]"""), "")
            .replace(Regex(""","radioFreq":\\d+"""), "")
        val decoded = decodeIslandState(older)
        assertNotNull(decoded)
        assertEquals(emptySet<String>(), decoded?.introducedActIds)
        assertEquals(4020, decoded?.radioFreq)
    }
}

private fun openIslandMemory(): IslandDatabase =
    Room.inMemoryDatabaseBuilder(PathContext(File("build")), IslandDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
