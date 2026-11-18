package com.tristamisland

import android.app.Application
import androidx.room.Room
import com.tristamisland.data.IslandDatabase
import com.tristamisland.data.IslandGameStore
import com.tristamisland.data.RoomIslandGameStore
import com.tristamisland.presentation.BackgroundMusic
import com.tristamisland.presentation.MusicPreference

class TristamApp : Application() {
    /** Process-scoped. [MainActivity] reports the foreground. The island screen reports the current track. */
    internal val backgroundMusic: BackgroundMusic by lazy { BackgroundMusic(this) }

    /** Stays off after the listener silences the music. A new region does not turn it back on. */
    internal val musicPreference: MusicPreference by lazy { MusicPreference(this) }

    private val islandDatabase: IslandDatabase by lazy {
        Room.databaseBuilder(this, IslandDatabase::class.java, ISLAND_DATABASE).build()
    }

    val islandGameStore: IslandGameStore by lazy {
        RoomIslandGameStore(islandDatabase.islandSaveDao())
    }

    private companion object {
        const val ISLAND_DATABASE: String = "tristam-island.db"
    }
}
