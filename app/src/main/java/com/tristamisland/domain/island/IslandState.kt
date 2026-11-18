package com.tristamisland.domain.island

import kotlinx.serialization.Serializable

@Serializable
data class IslandState(
    val storyId: String,
    val locationId: String,
    val visitedLocationIds: Set<String>,
    val inventory: List<String>,
    val flags: Map<String, Boolean>,
    val journalIds: Set<String>,
    val resolvedActionIds: List<String>,
    val prologueIndex: Int = 0,
    val score: Int = 0,
    val lastActionId: String? = null,
    val introducedActIds: Set<String> = emptySet(),
    val shoreId: String? = null,
    val seaHeading: Int? = null,
    val sandbankDepth: Int = 0,
    val lastResponse: String? = null,
    val placed: Map<String, List<String>> = emptyMap(),
    val woodDampness: Int = 5,
    val fireReady: Boolean = false,
    val hammockAt: String? = null,
    val radioFreq: Int = IslandRules.RADIO_START,
) {
    init {
        require(storyId.isNotBlank()) { "Story id must not be blank." }
        require(locationId.isNotBlank()) { "Location id must not be blank." }
        require(inventory.size <= IslandRules.MAX_CARRIED) {
            "Inventory holds at most ${IslandRules.MAX_CARRIED} items."
        }
        require(inventory.distinct().size == inventory.size) { "Carried item ids must be unique." }
        require(prologueIndex >= 0) { "Prologue index must be >= 0." }
        require(score >= 0) { "Score must be >= 0." }
        require(sandbankDepth >= 0) { "Sandbank depth must be >= 0." }
        require(seaHeading == null || seaHeading in 0..7) { "Sea heading must be 0..7." }
        require((shoreId == null) == (seaHeading == null)) { "Sea heading and shore travel together." }
        require(woodDampness >= 0) { "Wood dampness must be >= 0." }
        require(radioFreq in IslandRules.RADIO_LOW..IslandRules.RADIO_HIGH) {
            "Radio frequency must stay inside the band."
        }
        val grounded = placed.values.flatten()
        require(grounded.distinct().size == grounded.size) { "A grounded item has one place." }
        require(grounded.none { it in inventory }) { "An item cannot be carried and on the ground." }
    }
}

enum class IslandRejection {
    UNKNOWN_ACTION,
    WRONG_LOCATION,
    REQUIREMENTS_NOT_MET,
    ALREADY_RESOLVED,
    INVENTORY_FULL,
    DUPLICATE_ITEM,
    MISSING_ITEM,
    EFFECT_ILLEGAL,
}

sealed interface IslandResolution {
    val state: IslandState

    data class Accepted(
        override val state: IslandState,
        val response: String,
        val appliedEffects: List<IslandEffect>,
    ) : IslandResolution

    data class Rejected(
        override val state: IslandState,
        val reason: IslandRejection,
    ) : IslandResolution
}
