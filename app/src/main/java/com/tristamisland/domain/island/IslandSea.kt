package com.tristamisland.domain.island

/**
 * One ocean room. Headings follow the published source: index 0 is north and
 * the array runs clockwise. Inland is the shore's `seaInland`. Seaward is four
 * steps on. The sandbank, fish, and the two beaches that share a heading are
 * the same special cases as `MoveInSea`.
 */
internal enum class SeaMove {
    FOLLOW,
    ASHORE,
    BANK_OUT,
    BANK_BACK,
    TOO_DEEP,
    TOO_FAR,
    NOT_HERE,
}

internal data class SeaResult(
    val move: SeaMove,
    val state: IslandState,
    val response: String,
)

internal fun seaLocation(story: IslandStory): IslandLocation? =
    story.locations.firstOrNull { it.sea != null }

internal fun previewSea(story: IslandStory, state: IslandState, direction: Int): SeaMove =
    sail(story, state, direction).move

internal fun placeName(story: IslandStory, state: IslandState): String {
    val location = story.locations.first { it.id == state.locationId }
    val sea = location.sea ?: return location.name
    val sandbankHeading = story.locations.firstOrNull { it.sandbank }?.seaInland
    return if (state.seaHeading == sandbankHeading) sea.kneeDeepName else location.name
}

internal fun placePassage(story: IslandStory, state: IslandState): String {
    val location = story.locations.first { it.id == state.locationId }
    val sea = location.sea ?: return landPassage(location, state)
    val heading = state.seaHeading ?: return location.description
    val sandbankHeading = story.locations.firstOrNull { it.sandbank }?.seaInland
    if (heading == sandbankHeading) {
        val lines = mutableListOf(sea.sandbank)
        if (state.sandbankDepth > 0) lines += sea.tower
        if (state.sandbankDepth > sea.horizonDepth) lines += sea.horizon
        return lines.joinToString(" ")
    }
    val note = if (heading % sea.fishModulus == sea.fishRemainder) sea.fish else sea.barren
    return "${location.description} $note"
}

private fun landPassage(location: IslandLocation, state: IslandState): String {
    val text = location.description
    return when (location.id) {
        "outside_pub" -> {
            val door = if (state.flags["pub_unlocked"] == true) "abierta" else "cerrada"
            val stem = text.substringBefore(" La puerta del pub").trimEnd()
            "$stem La puerta del pub está $door."
        }
        "inside_pub" -> if (state.flags["generators_on"] == true) {
            text.replace("una gramola antigua, apagada.", "una gramola antigua, encendida.")
        } else {
            text
        }
        "scenic_view" -> if (state.flags["scenic_fire"] == true) {
            text.replace("quedan restos de una hoguera", "arde una hoguera")
        } else {
            text
        }
        "generators" -> if (state.flags["generators_on"] == true) {
            text.replace("y están apagadas.", "y están en marcha. Zumban.")
        } else {
            text
        }
        "white_house_porch" -> when {
            state.flags["mansion_entered"] == true ->
                text.replace("Sin luz, el teclado no responde.", "La puerta está abierta.")
            state.flags["generators_on"] == true ->
                text.replace("Sin luz, el teclado no responde.", "El teclado tiene luz.")
            else -> text
        }
        "white_house_lobby" -> if (state.flags["babel_solved"] == true) {
            text.replace("Al norte, una puerta alta, cerrada.", "Al norte, la puerta está abierta.")
        } else {
            text
        }
        "inside_elevator" -> if (state.flags["elevator_down"] == true) {
            text.replace("La puerta da a la antesala.", "La puerta da al vestíbulo del laboratorio.")
        } else {
            text
        }
        "lab_lobby" -> {
            val withoutBox = if (state.flags["filing_taken"] == true) {
                text.replace(" Sobre el escritorio hay una caja de cartón llena de papeles.", "")
            } else {
                text
            }
            if (state.flags["comms_open"] == true) {
                withoutBox.replace(
                    "Al oeste hay una puerta con la placa SALA DE COMUNICACIONES.",
                    "Al oeste, la puerta de la sala de comunicaciones está abierta.",
                )
            } else {
                withoutBox
            }
        }
        "lab_think" -> {
            val shelf = when {
                state.flags["chemistry_taken"] == true && state.flags["physics_taken"] == true ->
                    "En la estantería no queda ningún libro."
                state.flags["chemistry_taken"] == true -> "En la estantería queda un libro grueso."
                state.flags["physics_taken"] == true -> "En la estantería queda un libro enorme."
                else -> "En la estantería quedan dos libros: uno grueso y otro enorme."
            }
            text.replace("En la estantería quedan dos libros: uno grueso y otro enorme.", shelf)
        }
        "storage" -> when {
            state.flags["locker_open"] == true -> text.replace(
                "Contra la pared sur hay taquillas abiertas, menos una, con un candado de combinación.",
                "Contra la pared sur hay taquillas abiertas. La que estaba cerrada enseña un lío de cables.",
            )
            state.flags["locker_unlocked"] == true -> text.replace(
                "con un candado de combinación.",
                "con el candado ya suelto.",
            )
            else -> text
        }
        "major_door" -> if (state.flags["tile_pushed"] == true) {
            text.replace(
                "y el fluorescente no deja de parpadear.",
                "y una loseta del falso techo está apartada.",
            )
        } else {
            text
        }
        "comms_room" -> {
            val panel = when {
                state.flags["cable_fixed"] == true -> text.replace(
                    "El panel de acceso está cerrado.",
                    "El panel está abierto y el cable nuevo ya está puesto.",
                )
                state.flags["panel_open"] == true -> text.replace(
                    "El panel de acceso está cerrado.",
                    "El panel está abierto: dentro hay un cable derretido.",
                )
                else -> text
            }
            val dialled = panel.replace(
                "El dial marca 12.4020 Mc.",
                "El dial marca ${megacycles(state.radioFreq)}.",
            )
            if (state.flags["rescued"] == true) {
                dialled.replace(
                    "Por fin, la sala de comunicaciones.",
                    "La llamada ya ha salido. La sala queda en silencio.",
                )
            } else {
                dialled
            }
        }
        else -> text
    }
}

internal fun enterWater(story: IslandStory, state: IslandState): IslandState? {
    val from = story.locations.firstOrNull { it.id == state.locationId } ?: return null
    val inland = from.seaInland ?: return null
    val sea = seaLocation(story) ?: return null
    return state.copy(
        locationId = sea.id,
        visitedLocationIds = state.visitedLocationIds + sea.id,
        shoreId = from.id,
        seaHeading = inland,
        sandbankDepth = 0,
    )
}

internal fun sail(story: IslandStory, state: IslandState, direction: Int): SeaResult {
    val sea = seaLocation(story)
    val heading = state.seaHeading
    val shore = story.locations.firstOrNull { it.id == state.shoreId }
    if (sea == null || heading == null || shore == null || state.locationId != sea.id) {
        return SeaResult(SeaMove.NOT_HERE, state, "")
    }
    val copy = sea.sea ?: return SeaResult(SeaMove.NOT_HERE, state, "")
    val inland = heading
    val seaward = (heading + 4) % 8
    val sandbankHeading = story.locations.firstOrNull { it.sandbank }?.seaInland
    if (direction == seaward && heading == sandbankHeading) {
        if (state.sandbankDepth > copy.maxDepth) {
            return spoken(state, SeaMove.TOO_DEEP, copy.tooDeep)
        }
        return spoken(
            state.copy(sandbankDepth = state.sandbankDepth + 1),
            SeaMove.BANK_OUT,
            copy.bankOut,
        )
    }
    if (direction == inland && heading == sandbankHeading && state.sandbankDepth > 0) {
        return spoken(
            state.copy(sandbankDepth = state.sandbankDepth - 1),
            SeaMove.BANK_BACK,
            copy.bankBack,
        )
    }
    if (direction == inland) {
        return spoken(
            state.copy(
                locationId = shore.id,
                visitedLocationIds = state.visitedLocationIds + shore.id,
                shoreId = null,
                seaHeading = null,
                sandbankDepth = 0,
            ),
            SeaMove.ASHORE,
            copy.ashore,
        )
    }
    val along = (heading + 6) % 8
    val selected = if (direction == (heading + 7) % 8) along else direction
    val link = shore.coast.firstOrNull { it.direction == selected }
    if (link == null || selected == seaward) {
        return spoken(state, SeaMove.TOO_FAR, copy.tooFar)
    }
    val newHeading = when {
        selected == along && shore.holdSeaHeading != IslandRules.HOLD_CLOCKWISE -> (heading + 1) % 8
        selected != along && shore.holdSeaHeading != IslandRules.HOLD_COUNTERCLOCKWISE -> (heading + 7) % 8
        else -> heading
    }
    return spoken(
        state.copy(
            shoreId = link.locationId,
            seaHeading = newHeading,
            sandbankDepth = 0,
        ),
        SeaMove.FOLLOW,
        copy.follow,
    )
}

private fun spoken(state: IslandState, move: SeaMove, response: String): SeaResult =
    SeaResult(move, state.copy(lastResponse = response), response)
