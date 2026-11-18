package com.tristamisland.presentation

import com.tristamisland.data.IslandCommit
import com.tristamisland.data.IslandGameStore
import com.tristamisland.data.IslandRecord
import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.IslandResolution
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class IslandSnapshot(
    val record: IslandRecord? = null,
    val notice: String? = null,
)

/**
 * One island turn. The rules run inside the store lock's caller, and the
 * snapshot is replaced only after the write succeeds.
 */
class IslandSession(
    private val engine: IslandEngine,
    private val store: IslandGameStore,
) {
    private val gate = Mutex()
    private var snapshot = IslandSnapshot()

    suspend fun restore(): IslandSnapshot = gate.withLock {
        val loaded = store.load()
        if (loaded != null && !engine.canResume(loaded.state)) {
            store.discard()
            snapshot = IslandSnapshot()
            return@withLock snapshot
        }
        snapshot = IslandSnapshot(record = loaded)
        snapshot
    }

    suspend fun newGame(): IslandSnapshot = gate.withLock {
        val existing = store.load()
        if (existing != null) return@withLock IslandSnapshot(record = existing, notice = "Ya hay una partida guardada.")
        commitNew()
    }

    suspend fun replace(): IslandSnapshot = gate.withLock {
        val record = store.replace(engine.newGame())
        snapshot = IslandSnapshot(record = record)
        snapshot
    }

    private suspend fun commitNew(): IslandSnapshot {
        val committed = store.commit(engine.newGame(), expectedRevision = null)
        snapshot = when (committed) {
            is IslandCommit.Saved -> IslandSnapshot(record = committed.record)
            is IslandCommit.RevisionConflict -> IslandSnapshot(record = store.load(), notice = "Ya hay una partida guardada.")
        }
        return snapshot
    }

    suspend fun acknowledgeAct(): IslandSnapshot = gate.withLock {
        val record = snapshot.record ?: return@withLock snapshot
        apply(record) { engine.acknowledgeAct(it) }
    }

    suspend fun continuePrologue(): IslandSnapshot = gate.withLock {
        val record = snapshot.record ?: return@withLock snapshot
        apply(record) { engine.continuePrologue(it) }
    }

    suspend fun choose(actionId: String): IslandSnapshot = gate.withLock {
        val record = snapshot.record ?: return@withLock snapshot
        apply(record) { engine.resolve(it, actionId) }
    }

    suspend fun travelTo(locationId: String): IslandSnapshot = gate.withLock {
        val record = snapshot.record ?: return@withLock snapshot
        apply(record) { engine.travelTo(it, locationId) }
    }

    private suspend fun apply(
        record: IslandRecord,
        resolve: (com.tristamisland.domain.island.IslandState) -> IslandResolution,
    ): IslandSnapshot {
        val resolution = resolve(record.state)
        val next = when (resolution) {
            is IslandResolution.Rejected -> return IslandSnapshot(
                record = record,
                notice = rejectionNotice(resolution.reason.name),
            )
            is IslandResolution.Accepted -> resolution.state
        }
        if (next == record.state) return IslandSnapshot(record = record)
        snapshot = when (val committed = store.commit(next, record.revision)) {
            is IslandCommit.Saved -> IslandSnapshot(record = committed.record)
            is IslandCommit.RevisionConflict -> IslandSnapshot(
                record = store.load(),
                notice = "La partida guardada es más reciente.",
            )
        }
        return snapshot
    }
}

internal fun rejectionNotice(reason: String): String = when (reason) {
    "INVENTORY_FULL" -> "No te caben más cosas en las manos."
    "ALREADY_RESOLVED" -> "Eso ya lo has hecho."
    "REQUIREMENTS_NOT_MET" -> "Ahora mismo no puedes hacer eso."
    "MISSING_ITEM" -> "No llevas lo que hace falta."
    else -> "Eso no cambia nada."
}
