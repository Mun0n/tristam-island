package com.tristamisland.data

import com.tristamisland.domain.island.IslandState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomIslandGameStore(
    private val dao: IslandSaveDao,
) : IslandGameStore {
    private val gate = Mutex()

    override suspend fun load(): IslandRecord? = gate.withLock { read() }

    override suspend fun discard() {
        gate.withLock { dao.clear() }
    }

    override suspend fun commit(state: IslandState, expectedRevision: Int?): IslandCommit = gate.withLock {
        val current = read()
        val matches = if (expectedRevision == null) current == null else current?.revision == expectedRevision
        if (!matches) return@withLock IslandCommit.RevisionConflict
        val revision = if (current == null) 0 else current.revision + 1
        val record = IslandRecord(state = state, revision = revision)
        dao.write(
            IslandSaveEntity(
                revision = revision,
                stateJson = encodeIslandState(state),
            ),
        )
        IslandCommit.Saved(record)
    }

    override suspend fun replace(state: IslandState): IslandRecord = gate.withLock {
        val record = IslandRecord(state = state, revision = 0)
        dao.write(
            IslandSaveEntity(
                revision = 0,
                stateJson = encodeIslandState(state),
            ),
        )
        record
    }

    private suspend fun read(): IslandRecord? {
        val row = dao.read() ?: return null
        val state = decodeIslandState(row.stateJson) ?: run {
            dao.clear()
            return null
        }
        return IslandRecord(state = state, revision = row.revision)
    }
}
