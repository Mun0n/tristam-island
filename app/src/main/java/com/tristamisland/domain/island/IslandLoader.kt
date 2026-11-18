package com.tristamisland.domain.island

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class IslandLoadException(
    message: String,
) : IllegalArgumentException(message)

private val islandJson: Json = Json {
    ignoreUnknownKeys = false
    isLenient = false
    explicitNulls = false
    classDiscriminator = "type"
}

fun loadIsland(text: String): IslandStory {
    val story = try {
        islandJson.decodeFromString(IslandStory.serializer(), text)
    } catch (error: SerializationException) {
        throw IslandLoadException(error.message ?: "Island JSON could not be decoded.")
    }
    val errors = validateIsland(story)
    if (errors.isNotEmpty()) throw IslandLoadException(errors.joinToString("\n"))
    return story
}
