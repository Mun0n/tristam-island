package com.tristamisland.domain.island

fun validateIsland(story: IslandStory): List<String> {
    val errors = mutableListOf<String>()
    if (story.schemaVersion != IslandRules.SCHEMA_VERSION) {
        errors += "Schema version ${story.schemaVersion} is not supported. Expected ${IslandRules.SCHEMA_VERSION}."
    }
    if (story.title.isBlank()) errors += "Story title must not be blank."
    val ids = mutableMapOf<String, String>()
    fun id(owner: String, value: String) {
        if (!IslandRules.ID_PATTERN.matches(value)) {
            errors += "$owner id '$value' must match ${IslandRules.ID_PATTERN.pattern}."
            return
        }
        val previous = ids.put(value, owner)
        if (previous != null) errors += "Id '$value' is used by $previous and $owner."
    }
    id("story", story.id)
    story.acts.forEach { act ->
        id("act", act.id)
        if (act.title.isBlank()) errors += "Act '${act.id}' title must not be blank."
        if (!IslandRules.ID_PATTERN.matches(act.musicRegion)) {
            errors += "Act '${act.id}' music region '${act.musicRegion}' must match ${IslandRules.ID_PATTERN.pattern}."
        }
    }
    val actIds = story.acts.map { it.id }.toSet()
    story.locations.forEach { location ->
        id("location", location.id)
        if (location.name.isBlank()) errors += "Location '${location.id}' name must not be blank."
        if (location.description.isBlank()) errors += "Location '${location.id}' description must not be blank."
        if (location.actId.isBlank()) errors += "Location '${location.id}' act must not be blank."
        if (actIds.isNotEmpty() && location.actId !in actIds) {
            errors += "Location '${location.id}' act '${location.actId}' does not exist."
        }
        if (location.seaInland != null && location.seaInland !in 0..7) {
            errors += "Location '${location.id}' sea heading must be 0..7."
        }
        if (
            location.holdSeaHeading != null &&
            location.holdSeaHeading != IslandRules.HOLD_CLOCKWISE &&
            location.holdSeaHeading != IslandRules.HOLD_COUNTERCLOCKWISE
        ) {
            errors += "Location '${location.id}' hold '${location.holdSeaHeading}' is unknown."
        }
        if (location.sandbank && location.seaInland == null) {
            errors += "Location '${location.id}' sandbank needs a sea heading."
        }
        if (location.dryPerTurn < 0) {
            errors += "Location '${location.id}' drying must be >= 0."
        }
        val coastDirections = location.coast.map { it.direction }
        if (coastDirections.any { it !in 0..7 }) {
            errors += "Location '${location.id}' coast direction must be 0..7."
        }
        if (coastDirections.distinct().size != coastDirections.size) {
            errors += "Location '${location.id}' repeats a coast direction."
        }
        location.sea?.let { sea ->
            val seaText = listOf(
                sea.kneeDeepName, sea.sandbank, sea.tower, sea.horizon, sea.fish, sea.barren,
                sea.follow, sea.tooFar, sea.tooDeep, sea.bankOut, sea.bankBack, sea.ashore,
            )
            if (seaText.any { it.isBlank() }) {
                errors += "Location '${location.id}' sea text must not be blank."
            }
            if (sea.fishModulus <= 0 || sea.maxDepth < 0 || sea.horizonDepth < 0) {
                errors += "Location '${location.id}' sea limits must be zero or more."
            }
            if (sea.fishRemainder !in 0 until sea.fishModulus) {
                errors += "Location '${location.id}' fish remainder is outside the modulus."
            }
        }
    }
    if (story.locations.count { it.sea != null } > 1) {
        errors += "A story has one ocean."
    }
    if (story.locations.count { it.sandbank } > 1) {
        errors += "A story has one sandbank."
    }
    val locationIds = story.locations.map { it.id }.toSet()
    story.locations.forEach { location ->
        location.coast.filter { it.locationId !in locationIds }.forEach { link ->
            errors += "Location '${location.id}' coast leads to missing '${link.locationId}'."
        }
    }
    if (story.startLocationId !in locationIds) {
        errors += "Start location '${story.startLocationId}' does not exist."
    }
    story.items.forEach { item ->
        id("item", item.id)
        if (item.name.isBlank()) errors += "Item '${item.id}' name must not be blank."
        if (item.description.isBlank()) errors += "Item '${item.id}' description must not be blank."
    }
    val itemIds = story.items.map { it.id }.toSet()
    story.flags.forEach { flag -> id("flag", flag.id) }
    val flagIds = story.flags.map { it.id }.toSet()
    story.journal.forEach { entry ->
        id("journal", entry.id)
        if (entry.text.isBlank()) errors += "Journal '${entry.id}' text must not be blank."
    }
    val journalIds = story.journal.map { it.id }.toSet()
    story.actions.forEach { action -> id("action", action.id) }
    val actionIds = story.actions.map { it.id }.toSet()
    if (story.initialItemIds.size > IslandRules.MAX_CARRIED) {
        errors += "A new game carries at most ${IslandRules.MAX_CARRIED} items."
    }
    if (story.initialItemIds.distinct().size != story.initialItemIds.size) {
        errors += "Initial items must be unique."
    }
    story.prologue.forEachIndexed { index, paragraph ->
        if (paragraph.isBlank()) errors += "Prologue paragraph ${index + 1} must not be blank."
    }
    story.initialItemIds.filter { it !in itemIds }.forEach { missing ->
        errors += "Initial item '$missing' does not exist."
    }
    story.actions.forEach { action ->
        if (action.label.isBlank()) errors += "Action '${action.id}' label must not be blank."
        if (action.response.isBlank()) errors += "Action '${action.id}' response must not be blank."
        if (action.anywhere) {
            if (action.locationId.isNotEmpty() && action.locationId !in locationIds) {
                errors += "Action '${action.id}' location '${action.locationId}' does not exist."
            }
        } else if (action.locationId !in locationIds) {
            errors += "Action '${action.id}' location '${action.locationId}' does not exist."
        }
        action.equivalentActionIds.filter { it !in actionIds }.forEach { missing ->
            errors += "Action '${action.id}' names missing equivalent '$missing'."
        }
        checkRequirement(action.availability, flagIds, itemIds, "Action '${action.id}'")
            .forEach { errors += it }
        action.effects.forEach { effect ->
            when (effect) {
                is IslandEffect.SetFlag -> if (effect.key !in flagIds) {
                    errors += "Action '${action.id}' sets missing flag '${effect.key}'."
                }
                is IslandEffect.AddItem -> if (effect.itemId !in itemIds) {
                    errors += "Action '${action.id}' adds missing item '${effect.itemId}'."
                }
                is IslandEffect.RemoveItem -> if (effect.itemId !in itemIds) {
                    errors += "Action '${action.id}' removes missing item '${effect.itemId}'."
                }
                is IslandEffect.MoveTo -> if (effect.locationId !in locationIds) {
                    errors += "Action '${action.id}' moves to missing location '${effect.locationId}'."
                }
                is IslandEffect.AddJournal -> if (effect.entryId !in journalIds) {
                    errors += "Action '${action.id}' writes missing journal '${effect.entryId}'."
                }
                is IslandEffect.AddScore -> if (effect.amount == 0) {
                    errors += "Action '${action.id}' adds a score of zero."
                }
                IslandEffect.EnterWater -> {
                    if (story.locations.none { it.sea != null }) {
                        errors += "Action '${action.id}' enters water, and the story has no ocean."
                    }
                    val from = story.locations.firstOrNull { it.id == action.locationId }
                    if (from?.seaInland == null) {
                        errors += "Action '${action.id}' enters water from a place with no sea heading."
                    }
                }
                is IslandEffect.Sail -> if (effect.direction !in 0..7) {
                    errors += "Action '${action.id}' sails off the compass."
                }
                is IslandEffect.Tune -> if (effect.direction != "left" && effect.direction != "right") {
                    errors += "Action '${action.id}' tunes the radio off the knob."
                }
                IslandEffect.CallRadio -> Unit
                is IslandEffect.PlaceItem -> if (effect.itemId !in itemIds) {
                    errors += "Action '${action.id}' places missing item '${effect.itemId}'."
                }
                is IslandEffect.TakePlaced -> if (effect.itemId !in itemIds) {
                    errors += "Action '${action.id}' takes missing item '${effect.itemId}'."
                }
                is IslandEffect.RemovePlaced -> if (effect.itemId !in itemIds) {
                    errors += "Action '${action.id}' clears missing item '${effect.itemId}'."
                }
                IslandEffect.SetHammock, IslandEffect.ReadyFire, IslandEffect.ClearFire -> Unit
            }
        }
    }
    return errors
}

private fun checkRequirement(
    requirement: IslandRequirement,
    flagIds: Set<String>,
    itemIds: Set<String>,
    owner: String,
): List<String> {
    val errors = mutableListOf<String>()
    (requirement.flagIds + requirement.absentFlagIds).filter { it !in flagIds }.forEach { missing ->
        errors += "$owner requires missing flag '$missing'."
    }
    (requirement.itemIds + requirement.absentItemIds).filter { it !in itemIds }.forEach { missing ->
        errors += "$owner requires missing item '$missing'."
    }
    if (requirement.headings.any { it !in 0..7 }) {
        errors += "$owner heading must be 0..7."
    }
    if (requirement.minDepth != null && requirement.minDepth < 0) {
        errors += "$owner depth must be >= 0."
    }
    if (requirement.minWoodDampness != null && requirement.minWoodDampness < 0) {
        errors += "$owner wood dampness must be >= 0."
    }
    if (requirement.maxWoodDampness != null && requirement.maxWoodDampness < 0) {
        errors += "$owner wood dampness must be >= 0."
    }
    return errors
}
