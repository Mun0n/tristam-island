package com.tristamisland.data

import com.tristamisland.domain.island.IslandState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal val islandStateJson: Json = Json {
    ignoreUnknownKeys = false
    isLenient = false
    explicitNulls = false
    encodeDefaults = true
}

fun encodeIslandState(state: IslandState): String = islandStateJson.encodeToString(state)

fun decodeIslandState(text: String): IslandState? = try {
    islandStateJson.decodeFromString(IslandState.serializer(), text)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}
