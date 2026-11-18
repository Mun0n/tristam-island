package com.tristamisland.presentation

import com.tristamisland.domain.island.IslandEngine
import com.tristamisland.domain.island.IslandStory

enum class IslandPage {
    SCENE,
    JOURNAL,
    INVENTORY,
    STATUS,
}

enum class IslandMode {
    MENU,
    PLAY,
    GUIDE,
    CREDITS,
}

internal const val LOGBOOK_NAME: String = "Cuaderno de derrota"

internal data class IslandLeaf(
    val title: String,
    val paragraphs: List<String>,
    val folio: String,
)

internal val guideLeaf = IslandLeaf(
    title = "Cómo se juega",
    paragraphs = listOf(
        "Las decisiones están en la escena. Elige una y el juego responde siempre igual.",
        "Puedes llevar tres cosas en las manos. La partida se guarda sola al terminar cada acción.",
        "No hay conexión y no hay texto libre. Si una acción no aparece, ahora mismo no está a tu alcance.",
    ),
    folio = "pág. vi",
)

internal val creditsLeaf = IslandLeaf(
    title = "Créditos",
    paragraphs = listOf(
        "Tristam Island es un juego de Hugo Labrande, publicado como CC0 1.0.",
        "Esta adaptación es una obra aparte: texto en español, decisiones en pantalla y partida local. " +
            "No incluye la portada original ni el código de PunyInform.",
        "El capítulo jugable es la costa baja. El resto de la isla queda fuera de esta versión.",
        "En el menú suena Island on the Horizon. En la costa baja, La costa baja.",
    ),
    folio = "pág. vii",
)

data class IslandChoice(
    val id: String,
    val label: String,
)

data class IslandBelonging(
    val name: String,
    val description: String,
)

data class IslandNote(
    val id: String,
    val place: String,
    val text: String,
)

data class IslandPlay(
    val inPrologue: Boolean = false,
    val actTitle: String = "",
    val heading: String = "",
    val passage: String = "",
    val response: String? = null,
    val actions: List<IslandChoice> = emptyList(),
    val carried: List<IslandBelonging> = emptyList(),
    val journal: List<IslandNote> = emptyList(),
    val score: Int = 0,
    val visited: Int = 0,
    val places: Int = 0,
    val placeName: String = "",
    val musicRegion: String = "",
    val locationId: String = "",
    val prologueIndex: Int = 0,
    val actCard: String? = null,
    val chart: IslandChart = IslandChart(),
)

data class IslandUi(
    val mode: IslandMode = IslandMode.MENU,
    val page: IslandPage = IslandPage.SCENE,
    val ready: Boolean = false,
    val busy: Boolean = false,
    val hasSave: Boolean = false,
    val confirmNew: Boolean = false,
    val notice: String? = null,
    val play: IslandPlay = IslandPlay(),
)

private fun displayedActId(locationActId: String, locationId: String, elevatorDown: Boolean): String =
    if (elevatorDown && locationId == "inside_elevator") "act_4" else locationActId

fun presentIsland(story: IslandStory, engine: IslandEngine, snapshot: IslandSnapshot): IslandPlay {
    val state = snapshot.record?.state ?: return IslandPlay(places = story.locations.size)
    if (!engine.canResume(state)) return IslandPlay(places = story.locations.size)
    val location = story.locations.first { it.id == state.locationId }
    val actId = displayedActId(location.actId, location.id, state.flags["elevator_down"] == true)
    val act = story.acts.firstOrNull { it.id == actId }
    val actTitle = act?.title.orEmpty()
    val actOrdinal = story.acts.indexOfFirst { it.id == actId }
    val actCard = if (act != null && actOrdinal >= 0 && act.id !in state.introducedActIds) {
        "Acto ${actOrdinal + 1}"
    } else {
        null
    }
    val inPrologue = engine.inPrologue(state)
    val last = story.actions.firstOrNull { it.id == state.lastActionId }
    return IslandPlay(
        inPrologue = inPrologue,
        actTitle = actTitle,
        heading = if (inPrologue) story.title else engine.placeName(state),
        passage = if (inPrologue) engine.prologueText(state).orEmpty() else engine.passage(state),
        response = if (inPrologue) null else state.lastResponse ?: last?.response,
        actions = engine.availableActions(state).map { IslandChoice(it.id, it.label) },
        carried = state.inventory.mapNotNull { id ->
            story.items.firstOrNull { it.id == id }?.let { IslandBelonging(it.name, it.description) }
        },
        journal = story.journal.filter { it.id in state.journalIds }.map { entry ->
            IslandNote(entry.id, entry.place, entry.text)
        },
        score = state.score,
        visited = state.visitedLocationIds.size,
        places = story.locations.size,
        placeName = if (inPrologue) location.name else engine.placeName(state),
        musicRegion = act?.musicRegion.orEmpty(),
        locationId = state.locationId,
        prologueIndex = state.prologueIndex,
        actCard = actCard,
        chart = progressChart(
            visited = state.visitedLocationIds,
            here = state.locationId,
            shoreId = state.shoreId,
            seaInland = story.locations.firstOrNull { it.id == state.shoreId }?.seaInland,
            names = story.locations.associate { it.id to it.name },
            canEnterWater = location.seaInland != null,
            elevatorDown = state.flags["elevator_down"] == true,
        ),
    )
}
