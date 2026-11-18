package com.tristamisland.domain.island

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Schema 2 story. A missing clock, a missing death ending, and a missing
 * character are absent rules. They are not filled with placeholder values.
 */
@Serializable
data class IslandStory(
    val schemaVersion: Int,
    val id: String,
    val title: String,
    val startLocationId: String,
    val locations: List<IslandLocation>,
    val acts: List<IslandAct> = emptyList(),
    val items: List<IslandItem> = emptyList(),
    val flags: List<IslandFlag> = emptyList(),
    val actions: List<IslandAction> = emptyList(),
    val journal: List<IslandJournalEntry> = emptyList(),
    val initialItemIds: List<String> = emptyList(),
    val prologue: List<String> = emptyList(),
)

@Serializable
data class IslandAct(
    val id: String,
    val title: String,
    val musicRegion: String,
)

@Serializable
data class IslandLocation(
    val id: String,
    val name: String,
    val description: String,
    val actId: String,
    /** Inland compass index while this shore is the water's reference. Null stays on land. */
    val seaInland: Int? = null,
    val sandbank: Boolean = false,
    /** `clockwise` or `counterclockwise`: moving that way along this shore keeps the heading. */
    val holdSeaHeading: String? = null,
    val coast: List<IslandCoastLink> = emptyList(),
    val sea: IslandSeaCopy? = null,
    /** How much firewood on the ground dries each turn. Woods stay at zero. */
    val dryPerTurn: Int = 0,
)

@Serializable
data class IslandCoastLink(
    val direction: Int,
    val locationId: String,
)

/**
 * Prose and limits for the one ocean room. The numbers match the published source:
 * fish where the heading modulo [fishModulus] is [fishRemainder], the horizon after
 * [horizonDepth], and a sandbank step that stops once the depth is past [maxDepth].
 */
@Serializable
data class IslandSeaCopy(
    val kneeDeepName: String,
    val sandbank: String,
    val tower: String,
    val horizon: String,
    val fish: String,
    val barren: String,
    val follow: String,
    val tooFar: String,
    val tooDeep: String,
    val bankOut: String,
    val bankBack: String,
    val ashore: String,
    val fishModulus: Int,
    val fishRemainder: Int,
    val horizonDepth: Int,
    val maxDepth: Int,
)

@Serializable
data class IslandItem(
    val id: String,
    val name: String,
    val description: String = "",
)

@Serializable
data class IslandFlag(
    val id: String,
    @SerialName("default")
    val defaultValue: Boolean = false,
)

@Serializable
data class IslandJournalEntry(
    val id: String,
    val text: String,
    val place: String = "",
)

/**
 * Every listed constraint must hold. An empty requirement always holds.
 */
@Serializable
data class IslandRequirement(
    val flagIds: List<String> = emptyList(),
    val absentFlagIds: List<String> = emptyList(),
    val itemIds: List<String> = emptyList(),
    val absentItemIds: List<String> = emptyList(),
    val headings: List<Int> = emptyList(),
    val minDepth: Int? = null,
    val landOnly: Boolean = false,
    val placedItemIds: List<String> = emptyList(),
    val minWoodDampness: Int? = null,
    val maxWoodDampness: Int? = null,
    val needsFire: Boolean = false,
    val needsNoFire: Boolean = false,
    val hammockHere: Boolean = false,
    val minDryPerTurn: Int? = null,
)

@Serializable
data class IslandAction(
    val id: String,
    val label: String,
    val locationId: String = "",
    val anywhere: Boolean = false,
    val availability: IslandRequirement = IslandRequirement(),
    val effects: List<IslandEffect> = emptyList(),
    val response: String,
    val repeatable: Boolean = false,
    val equivalentActionIds: List<String> = emptyList(),
)

@Serializable
sealed interface IslandEffect {
    @Serializable
    @SerialName("SET_FLAG")
    data class SetFlag(
        val key: String,
        val value: Boolean,
    ) : IslandEffect

    @Serializable
    @SerialName("ADD_ITEM")
    data class AddItem(
        val itemId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("REMOVE_ITEM")
    data class RemoveItem(
        val itemId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("MOVE_TO")
    data class MoveTo(
        val locationId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("ADD_JOURNAL")
    data class AddJournal(
        val entryId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("ADD_SCORE")
    data class AddScore(
        val amount: Int,
    ) : IslandEffect

    @Serializable
    @SerialName("ENTER_WATER")
    data object EnterWater : IslandEffect

    /** Compass index, clockwise from north: 0 north through 7 northwest. */
    @Serializable
    @SerialName("SAIL")
    data class Sail(
        val direction: Int,
    ) : IslandEffect

    @Serializable
    @SerialName("PLACE_ITEM")
    data class PlaceItem(
        val itemId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("TAKE_PLACED")
    data class TakePlaced(
        val itemId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("REMOVE_PLACED")
    data class RemovePlaced(
        val itemId: String,
    ) : IslandEffect

    @Serializable
    @SerialName("SET_HAMMOCK")
    data object SetHammock : IslandEffect

    @Serializable
    @SerialName("READY_FIRE")
    data object ReadyFire : IslandEffect

    @Serializable
    @SerialName("CLEAR_FIRE")
    data object ClearFire : IslandEffect

    /** "left" subtracts 13. "right" adds 20. The band and the coast-guard mark are in IslandRules. */
    @Serializable
    @SerialName("TUNE")
    data class Tune(
        val direction: String,
    ) : IslandEffect

    @Serializable
    @SerialName("CALL_RADIO")
    data object CallRadio : IslandEffect
}

object IslandRules {
    const val SCHEMA_VERSION: Int = 2

    /** Hands, as declared in the original source. The backpack is a later object. */
    const val MAX_CARRIED: Int = 3

    val ID_PATTERN: Regex = Regex("^[a-z][a-z0-9_]*$")
    const val HOLD_CLOCKWISE: String = "clockwise"
    const val HOLD_COUNTERCLOCKWISE: String = "counterclockwise"
    const val FIREWOOD: String = "firewood"

    /** The published source arranges the fire when dampness is below this. */
    const val WOOD_READY_BELOW: Int = 2

    /** Dial units in the published radio. Shown to the player as 12.xxxx Mc. */
    const val RADIO_START: Int = 4020
    const val RADIO_COAST: Int = 4035
    const val RADIO_LOW: Int = 3895
    const val RADIO_HIGH: Int = 4126
    const val RADIO_LEFT: Int = 13
    const val RADIO_RIGHT: Int = 20
}

fun IslandRequirement.holds(
    flags: Map<String, Boolean>,
    inventory: List<String>,
    seaHeading: Int?,
    sandbankDepth: Int,
    placedHere: Set<String> = emptySet(),
    woodDampness: Int = 5,
    fireReady: Boolean = false,
    hammockAtHere: Boolean = false,
    dryPerTurn: Int = 0,
): Boolean =
    flagIds.all { flags[it] == true } &&
        absentFlagIds.all { flags[it] != true } &&
        itemIds.all { it in inventory } &&
        absentItemIds.all { it !in inventory } &&
        (headings.isEmpty() || seaHeading in headings) &&
        (minDepth == null || sandbankDepth >= minDepth) &&
        (!landOnly || seaHeading == null) &&
        placedItemIds.all { it in placedHere } &&
        (minWoodDampness == null || woodDampness >= minWoodDampness) &&
        (maxWoodDampness == null || woodDampness <= maxWoodDampness) &&
        (!needsFire || fireReady) &&
        (!needsNoFire || !fireReady) &&
        (!hammockHere || hammockAtHere) &&
        (minDryPerTurn == null || dryPerTurn >= minDryPerTurn)
