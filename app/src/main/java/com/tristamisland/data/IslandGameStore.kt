package com.tristamisland.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tristamisland.domain.island.IslandState

@Entity(tableName = "island_save")
data class IslandSaveEntity(
    @PrimaryKey val slot: Int = SLOT,
    val revision: Int,
    val stateJson: String,
) {
    companion object {
        const val SLOT: Int = 1
    }
}

data class IslandRecord(
    val state: IslandState,
    val revision: Int,
)

sealed interface IslandCommit {
    data class Saved(val record: IslandRecord) : IslandCommit
    data object RevisionConflict : IslandCommit
}

/**
 * One confirmed island snapshot.
 * The revision check and the write share one lock.
 */
interface IslandGameStore {
    suspend fun load(): IslandRecord?

    suspend fun discard()

    suspend fun commit(state: IslandState, expectedRevision: Int?): IslandCommit

    suspend fun replace(state: IslandState): IslandRecord
}

@Dao
interface IslandSaveDao {
    @Query("SELECT * FROM island_save WHERE slot = 1")
    suspend fun read(): IslandSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun write(entity: IslandSaveEntity)

    @Query("DELETE FROM island_save")
    suspend fun clear()
}
