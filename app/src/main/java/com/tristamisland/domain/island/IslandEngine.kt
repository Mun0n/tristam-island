package com.tristamisland.domain.island

/**
 * Resolves one authored island action.
 * A rejection returns the same state instance. Nothing here rolls a die,
 * advances a clock, or calls out of the device.
 */
class IslandEngine(
    val story: IslandStory,
) {
    private val actions = story.actions.associateBy { it.id }
    private val items = story.items.associateBy { it.id }
    private val flags = story.flags.associateBy { it.id }
    private val locations = story.locations.associateBy { it.id }
    private val journal = story.journal.associateBy { it.id }

    init {
        val errors = validateIsland(story)
        check(errors.isEmpty()) { errors.joinToString("\n") }
    }

    fun newGame(): IslandState = IslandState(
        storyId = story.id,
        locationId = story.startLocationId,
        visitedLocationIds = setOf(story.startLocationId),
        inventory = story.initialItemIds,
        flags = story.flags.associate { it.id to it.defaultValue },
        journalIds = emptySet(),
        resolvedActionIds = emptyList(),
        prologueIndex = 0,
        score = 0,
        lastActionId = null,
    )

    fun canResume(state: IslandState): Boolean =
        state.storyId == story.id &&
            state.locationId in locations &&
            state.prologueIndex <= story.prologue.size &&
            state.inventory.all { it in items } &&
            state.journalIds.all { it in journal } &&
            state.flags.keys.all { it in flags } &&
            state.resolvedActionIds.all { it in actions } &&
            state.introducedActIds.all { it in story.acts.map { act -> act.id } } &&
            (state.lastActionId == null || state.lastActionId in actions) &&
            seaStateHolds(state) &&
            placedHolds(state)

    fun inPrologue(state: IslandState): Boolean {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        return state.prologueIndex < story.prologue.size
    }

    fun prologueText(state: IslandState): String? {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        return story.prologue.getOrNull(state.prologueIndex)
    }

    fun acknowledgeAct(state: IslandState): IslandResolution {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        val actId = locations[state.locationId]?.actId ?: return reject(state, IslandRejection.EFFECT_ILLEGAL)
        val act = story.acts.firstOrNull { it.id == actId } ?: return reject(state, IslandRejection.EFFECT_ILLEGAL)
        if (actId in state.introducedActIds) return reject(state, IslandRejection.ALREADY_RESOLVED)
        return IslandResolution.Accepted(
            state = state.copy(introducedActIds = state.introducedActIds + actId),
            response = act.title,
            appliedEffects = emptyList(),
        )
    }

    fun continuePrologue(state: IslandState): IslandResolution {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        val paragraph = story.prologue.getOrNull(state.prologueIndex)
            ?: return reject(state, IslandRejection.ALREADY_RESOLVED)
        return IslandResolution.Accepted(
            state = state.copy(prologueIndex = state.prologueIndex + 1),
            response = paragraph,
            appliedEffects = emptyList(),
        )
    }

    fun availableActions(state: IslandState): List<IslandAction> {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        if (inPrologue(state)) return emptyList()
        return story.actions.filter { action ->
            (action.anywhere || action.locationId == state.locationId) &&
                (action.repeatable || action.id !in state.resolvedActionIds) &&
                holds(state, action) &&
                sailShown(state, action)
        }
    }

    fun placeName(state: IslandState): String = com.tristamisland.domain.island.placeName(story, state)

    fun passage(state: IslandState): String = placePassage(story, state)

    /**
     * Moves to a place the player has already visited. The ocean is entered from
     * the shore they are standing on. An unknown or unvisited place is rejected.
     */
    fun travelTo(state: IslandState, locationId: String): IslandResolution {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        if (inPrologue(state)) return reject(state, IslandRejection.REQUIREMENTS_NOT_MET)
        val destination = locations[locationId] ?: return reject(state, IslandRejection.UNKNOWN_ACTION)
        if (locationId !in state.visitedLocationIds) return reject(state, IslandRejection.REQUIREMENTS_NOT_MET)
        val woodWasDown = woodPlace(state)
        val moved = if (destination.sea != null) {
            enterWater(story, state) ?: return reject(state, IslandRejection.REQUIREMENTS_NOT_MET)
        } else if (state.locationId == locationId && state.seaHeading == null) {
            return reject(state, IslandRejection.ALREADY_RESOLVED)
        } else {
            state.copy(
                locationId = locationId,
                visitedLocationIds = state.visitedLocationIds + locationId,
                shoreId = null,
                seaHeading = null,
                sandbankDepth = 0,
            )
        }
        val spoken = if (destination.sea != null) "Te metes en el agua." else "Te diriges a ${destination.name}."
        val unplugged = if (
            state.locationId == "comms_room" &&
            locationId != "comms_room" &&
            moved.flags["headphones_on"] == true
        ) {
            moved.copy(flags = moved.flags + ("headphones_on" to false))
        } else {
            moved
        }
        return IslandResolution.Accepted(
            state = dryWood(woodWasDown, unplugged).copy(lastResponse = spoken),
            response = spoken,
            appliedEffects = emptyList(),
        )
    }

    fun resolve(state: IslandState, actionId: String): IslandResolution {
        require(state.storyId == story.id) { "Wrong story: ${state.storyId}." }
        if (state.prologueIndex < story.prologue.size) return reject(state, IslandRejection.REQUIREMENTS_NOT_MET)
        val action = actions[actionId] ?: return reject(state, IslandRejection.UNKNOWN_ACTION)
        if (!action.anywhere && state.locationId != action.locationId) {
            return reject(state, IslandRejection.WRONG_LOCATION)
        }
        if (!action.repeatable && action.id in state.resolvedActionIds) {
            return reject(state, IslandRejection.ALREADY_RESOLVED)
        }
        if (!holds(state, action)) {
            return reject(state, IslandRejection.REQUIREMENTS_NOT_MET)
        }
        val woodWasDown = woodPlace(state)
        val draft = Draft(state)
        for (effect in action.effects) {
            val reason = draft.apply(effect) ?: continue
            return reject(state, reason)
        }
        val resolved = if (action.repeatable) draft.state.resolvedActionIds else {
            draft.state.resolvedActionIds + action.id
        }
        val spoken = draft.spoken ?: action.response
        val dried = dryWood(woodWasDown, draft.state)
        return IslandResolution.Accepted(
            state = dried.copy(
                resolvedActionIds = resolved,
                lastActionId = action.id,
                lastResponse = spoken,
            ),
            response = spoken,
            appliedEffects = action.effects,
        )
    }

    private fun seaStateHolds(state: IslandState): Boolean {
        val sea = seaLocation(story)
        if (state.seaHeading == null) return state.sandbankDepth == 0 && state.locationId != sea?.id
        if (sea == null || state.locationId != sea.id || state.shoreId == null) return false
        val shore = locations[state.shoreId] ?: return false
        return shore.seaInland == state.seaHeading
    }

    private fun sailShown(state: IslandState, action: IslandAction): Boolean {
        val sail = action.effects.filterIsInstance<IslandEffect.Sail>().singleOrNull() ?: return true
        return previewSea(story, state, sail.direction) != SeaMove.TOO_FAR &&
            previewSea(story, state, sail.direction) != SeaMove.NOT_HERE
    }

    private fun reject(state: IslandState, reason: IslandRejection): IslandResolution =
        IslandResolution.Rejected(state = state, reason = reason)

    private fun Draft.apply(effect: IslandEffect): IslandRejection? = when (effect) {
        is IslandEffect.SetFlag -> setFlag(effect)
        is IslandEffect.AddItem -> addItem(effect)
        is IslandEffect.RemoveItem -> removeItem(effect)
        is IslandEffect.MoveTo -> moveTo(effect)
        is IslandEffect.AddJournal -> addJournal(effect)
        is IslandEffect.AddScore -> addScore(effect)
        IslandEffect.EnterWater -> enterWater()
        is IslandEffect.Sail -> sailToward(effect)
        is IslandEffect.Tune -> tune(effect)
        IslandEffect.CallRadio -> call()
        is IslandEffect.PlaceItem -> placeItem(effect)
        is IslandEffect.TakePlaced -> takePlaced(effect)
        is IslandEffect.RemovePlaced -> removePlaced(effect)
        IslandEffect.SetHammock -> setHammock()
        IslandEffect.ReadyFire -> readyFire()
        IslandEffect.ClearFire -> {
            state = state.copy(fireReady = false)
            null
        }
    }

    private fun holds(state: IslandState, action: IslandAction): Boolean =
        action.availability.holds(
            flags = state.flags,
            inventory = state.inventory,
            seaHeading = state.seaHeading,
            sandbankDepth = state.sandbankDepth,
            placedHere = state.placed[state.locationId].orEmpty().toSet(),
            woodDampness = state.woodDampness,
            fireReady = state.fireReady,
            hammockAtHere = state.hammockAt == state.locationId,
            dryPerTurn = locations[state.locationId]?.dryPerTurn ?: 0,
        )

    private fun placedHolds(state: IslandState): Boolean {
        if (state.woodDampness < 0) return false
        if (state.hammockAt != null && state.hammockAt !in locations) return false
        return state.placed.all { (place, things) ->
            place in locations && things.all { it in items } && things.none { it in state.inventory }
        }
    }

    private fun woodPlace(state: IslandState): String? =
        state.placed.entries.firstOrNull { IslandRules.FIREWOOD in it.value }?.key

    private fun dryWood(where: String?, state: IslandState): IslandState {
        if (where == null || state.fireReady || state.woodDampness == 0) return state
        if (state.placed[where]?.contains(IslandRules.FIREWOOD) != true) return state
        val rate = locations[where]?.dryPerTurn ?: 0
        if (rate == 0) return state
        return state.copy(woodDampness = (state.woodDampness - rate).coerceAtLeast(0))
    }

    private fun Draft.setFlag(effect: IslandEffect.SetFlag): IslandRejection? {
        if (effect.key !in flags) return IslandRejection.EFFECT_ILLEGAL
        state = state.copy(flags = state.flags + (effect.key to effect.value))
        return null
    }

    private fun Draft.addItem(effect: IslandEffect.AddItem): IslandRejection? {
        if (effect.itemId !in items) return IslandRejection.EFFECT_ILLEGAL
        if (state.inventory.any { it == effect.itemId }) return IslandRejection.DUPLICATE_ITEM
        if (state.inventory.size >= IslandRules.MAX_CARRIED) return IslandRejection.INVENTORY_FULL
        state = state.copy(inventory = state.inventory + effect.itemId)
        return null
    }

    private fun Draft.removeItem(effect: IslandEffect.RemoveItem): IslandRejection? {
        if (effect.itemId !in items) return IslandRejection.EFFECT_ILLEGAL
        if (effect.itemId !in state.inventory) return IslandRejection.MISSING_ITEM
        state = state.copy(inventory = state.inventory.filterNot { it == effect.itemId })
        return null
    }

    private fun Draft.moveTo(effect: IslandEffect.MoveTo): IslandRejection? {
        val destination = locations[effect.locationId] ?: return IslandRejection.EFFECT_ILLEGAL
        if (destination.sea != null) return IslandRejection.EFFECT_ILLEGAL
        state = state.copy(
            locationId = effect.locationId,
            visitedLocationIds = state.visitedLocationIds + effect.locationId,
            shoreId = null,
            seaHeading = null,
            sandbankDepth = 0,
        )
        return null
    }

    private fun Draft.enterWater(): IslandRejection? {
        val next = enterWater(story, state) ?: return IslandRejection.EFFECT_ILLEGAL
        state = next
        return null
    }

    private fun Draft.sailToward(effect: IslandEffect.Sail): IslandRejection? {
        if (effect.direction !in 0..7) return IslandRejection.EFFECT_ILLEGAL
        val result = sail(story, state, effect.direction)
        if (result.move == SeaMove.NOT_HERE) return IslandRejection.EFFECT_ILLEGAL
        state = result.state
        spoken = result.response
        return null
    }

    private fun Draft.placeItem(effect: IslandEffect.PlaceItem): IslandRejection? {
        if (effect.itemId !in items) return IslandRejection.EFFECT_ILLEGAL
        if (locations[state.locationId]?.sea != null) return IslandRejection.EFFECT_ILLEGAL
        if (effect.itemId !in state.inventory) return IslandRejection.MISSING_ITEM
        val here = state.placed[state.locationId].orEmpty()
        state = state.copy(
            inventory = state.inventory - effect.itemId,
            placed = state.placed + (state.locationId to here + effect.itemId),
        )
        return null
    }

    private fun Draft.takePlaced(effect: IslandEffect.TakePlaced): IslandRejection? {
        if (effect.itemId !in items) return IslandRejection.EFFECT_ILLEGAL
        val here = state.placed[state.locationId].orEmpty()
        if (effect.itemId !in here) return IslandRejection.MISSING_ITEM
        if (state.inventory.size >= IslandRules.MAX_CARRIED) return IslandRejection.INVENTORY_FULL
        val left = here - effect.itemId
        val placed = if (left.isEmpty()) state.placed - state.locationId else state.placed + (state.locationId to left)
        state = state.copy(inventory = state.inventory + effect.itemId, placed = placed)
        return null
    }

    private fun Draft.removePlaced(effect: IslandEffect.RemovePlaced): IslandRejection? {
        if (effect.itemId !in items) return IslandRejection.EFFECT_ILLEGAL
        val here = state.placed[state.locationId].orEmpty()
        if (effect.itemId !in here) return IslandRejection.MISSING_ITEM
        val left = here - effect.itemId
        val placed = if (left.isEmpty()) state.placed - state.locationId else state.placed + (state.locationId to left)
        state = state.copy(placed = placed)
        return null
    }

    private fun Draft.setHammock(): IslandRejection? {
        if (locations[state.locationId]?.sea != null) return IslandRejection.EFFECT_ILLEGAL
        state = state.copy(hammockAt = state.locationId)
        return null
    }

    private fun Draft.readyFire(): IslandRejection? {
        if (state.woodDampness >= IslandRules.WOOD_READY_BELOW) return IslandRejection.EFFECT_ILLEGAL
        if (state.placed[state.locationId]?.contains(IslandRules.FIREWOOD) != true) {
            return IslandRejection.EFFECT_ILLEGAL
        }
        state = state.copy(fireReady = true)
        return null
    }

    private fun Draft.addJournal(effect: IslandEffect.AddJournal): IslandRejection? {
        if (effect.entryId !in journal) return IslandRejection.EFFECT_ILLEGAL
        state = state.copy(journalIds = state.journalIds + effect.entryId)
        return null
    }

    private fun Draft.tune(effect: IslandEffect.Tune): IslandRejection? {
        val turn = tuneRadio(state, effect.direction) ?: return IslandRejection.EFFECT_ILLEGAL
        return applyRadio(turn)
    }

    private fun Draft.call(): IslandRejection? = applyRadio(callRadio(state))

    private fun Draft.applyRadio(turn: RadioResult): IslandRejection? {
        state = turn.state
        spoken = turn.response
        if (turn.flag != null) {
            setFlag(IslandEffect.SetFlag(turn.flag, true))?.let { return it }
        }
        if (turn.journalId != null) {
            addJournal(IslandEffect.AddJournal(turn.journalId))?.let { return it }
        }
        if (turn.score != 0) {
            addScore(IslandEffect.AddScore(turn.score))?.let { return it }
        }
        return null
    }

    private fun Draft.addScore(effect: IslandEffect.AddScore): IslandRejection? {
        val next = state.score.toLong() + effect.amount
        if (next !in 0..Int.MAX_VALUE.toLong()) return IslandRejection.EFFECT_ILLEGAL
        state = state.copy(score = next.toInt())
        return null
    }

    private class Draft(
        var state: IslandState,
        var spoken: String? = null,
    )
}
