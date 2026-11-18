package com.tristamisland.presentation

/**
 * Schematic of the island. A place appears after the player has been there.
 * The village sits above the coast. The ocean stays one mark, offshore of the shore they face.
 */
data class ChartMark(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val here: Boolean,
)

data class ChartNote(
    val id: String,
    val name: String,
    val canTravel: Boolean = true,
)

data class IslandChart(
    val marks: List<ChartMark> = emptyList(),
    val links: List<Pair<Int, Int>> = emptyList(),
    val elsewhere: List<ChartNote> = emptyList(),
)

private data class ChartSpot(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
)

private val coastSpots = listOf(
    ChartSpot("scenic_view", "Mirador", 0.88f, 0.05f),
    ChartSpot("outside_storehouse", "Almacén", 0.16f, 0.06f),
    ChartSpot("cliff_house", "Casa norte", 0.40f, 0.10f),
    ChartSpot("shop", "Tienda", 0.64f, 0.08f),
    ChartSpot("community_garden", "Huerto", 0.32f, 0.16f),
    ChartSpot("woodshop", "Taller", 0.86f, 0.16f),
    ChartSpot("pub_landlord_house", "Casa", 0.14f, 0.18f),
    ChartSpot("outside_pub", "Pub", 0.50f, 0.18f),
    ChartSpot("white_house_porch", "Porche", 0.94f, 0.24f),
    ChartSpot("generators", "Motores", 0.72f, 0.24f),
    ChartSpot("base_of_tower", "Torre", 0.06f, 0.30f),
    ChartSpot("top_of_tower", "Cima", 0.18f, 0.36f),
    ChartSpot("central_plaza", "Plaza", 0.52f, 0.28f),
    ChartSpot("field", "Campo", 0.86f, 0.32f),
    ChartSpot("houses", "Casas", 0.34f, 0.32f),
    ChartSpot("fence_gate", "Verja", 0.68f, 0.34f),
    ChartSpot("around_fence", "Borde", 0.52f, 0.34f),
    ChartSpot("top_of_stairs", "Escalera", 0.34f, 0.36f),
    ChartSpot("northwestern_cliffs", "Noroeste", 0.16f, 0.66f),
    ChartSpot("northern_cliffs", "Muelle", 0.42f, 0.70f),
    ChartSpot("northeastern_tip", "Punta", 0.66f, 0.68f),
    ChartSpot("narrow_strait", "Estrecho", 0.08f, 0.72f),
    ChartSpot("deep_woods", "Bosque", 0.28f, 0.64f),
    ChartSpot("cove", "Cala", 0.82f, 0.62f),
    ChartSpot("edge_of_woods", "Linde", 0.30f, 0.70f),
    ChartSpot("southwestern_cliffs", "Suroeste", 0.12f, 0.76f),
    ChartSpot("end_of_beach", "Peñasco", 0.64f, 0.76f),
    ChartSpot("beach_by_forest", "Arena oeste", 0.28f, 0.86f),
    ChartSpot("immaculate_beach", "Playa sur", 0.50f, 0.90f),
)

private val coastLinks = listOf(
    "immaculate_beach" to "beach_by_forest",
    "immaculate_beach" to "end_of_beach",
    "end_of_beach" to "cove",
    "cove" to "northeastern_tip",
    "northeastern_tip" to "northern_cliffs",
    "northern_cliffs" to "northwestern_cliffs",
    "northwestern_cliffs" to "narrow_strait",
    "narrow_strait" to "southwestern_cliffs",
    "southwestern_cliffs" to "beach_by_forest",
    "beach_by_forest" to "edge_of_woods",
    "edge_of_woods" to "deep_woods",
    "deep_woods" to "top_of_stairs",
    "northern_cliffs" to "outside_storehouse",
    "outside_storehouse" to "community_garden",
    "outside_storehouse" to "outside_pub",
    "outside_storehouse" to "cliff_house",
    "community_garden" to "outside_pub",
    "community_garden" to "pub_landlord_house",
    "outside_pub" to "cliff_house",
    "outside_pub" to "central_plaza",
    "outside_pub" to "pub_landlord_house",
    "pub_landlord_house" to "base_of_tower",
    "base_of_tower" to "top_of_tower",
    "pub_landlord_house" to "houses",
    "central_plaza" to "shop",
    "central_plaza" to "houses",
    "central_plaza" to "fence_gate",
    "central_plaza" to "generators",
    "houses" to "top_of_stairs",
    "houses" to "around_fence",
    "houses" to "fence_gate",
    "cliff_house" to "shop",
    "shop" to "woodshop",
    "shop" to "scenic_view",
    "woodshop" to "generators",
    "generators" to "fence_gate",
    "generators" to "field",
    "fence_gate" to "around_fence",
    "around_fence" to "field",
    "field" to "white_house_porch",
)

private val mansionSpots = listOf(
    ChartSpot("yellow_room", "Amarilla", 0.34f, 0.08f),
    ChartSpot("inside_elevator", "Ascensor", 0.62f, 0.08f),
    ChartSpot("mezzanine", "Entreplanta", 0.10f, 0.26f),
    ChartSpot("quarters", "Dormitorios", 0.34f, 0.26f),
    ChartSpot("antechamber", "Antesala", 0.62f, 0.28f),
    ChartSpot("white_room", "Blanca", 0.34f, 0.44f),
    ChartSpot("white_house_porch", "Porche", 0.28f, 0.66f),
    ChartSpot("white_house_lobby", "Recibidor", 0.62f, 0.50f),
    ChartSpot("cafeteria", "Comedor", 0.88f, 0.50f),
    ChartSpot("mansion_library", "Biblioteca", 0.62f, 0.74f),
)

private val mansionLinks = listOf(
    "inside_elevator" to "antechamber",
    "antechamber" to "white_house_lobby",
    "yellow_room" to "quarters",
    "quarters" to "white_room",
    "quarters" to "mezzanine",
    "mezzanine" to "white_house_lobby",
    "white_house_lobby" to "cafeteria",
    "white_house_lobby" to "mansion_library",
    "white_house_lobby" to "white_house_porch",
)

private val labSpots = listOf(
    ChartSpot("major_office", "Despacho", 0.84f, 0.06f),
    ChartSpot("inside_elevator", "Ascensor", 0.28f, 0.08f),
    ChartSpot("major_door", "Mayor", 0.84f, 0.22f),
    ChartSpot("lab_lobby", "Vestíbulo", 0.28f, 0.28f),
    ChartSpot("comms_room", "Radio", 0.08f, 0.28f),
    ChartSpot("conference", "Reuniones", 0.56f, 0.28f),
    ChartSpot("hall_window", "Ventanal", 0.84f, 0.40f),
    ChartSpot("lab_hall", "Pasillo", 0.28f, 0.52f),
    ChartSpot("lab_cross", "Cruce", 0.56f, 0.52f),
    ChartSpot("storage", "Taquillas", 0.84f, 0.52f),
    ChartSpot("lab_bench", "Laboratorio", 0.28f, 0.76f),
    ChartSpot("lab_think", "Pizarra", 0.56f, 0.76f),
)

private val labLinks = listOf(
    "major_office" to "major_door",
    "major_door" to "hall_window",
    "inside_elevator" to "lab_lobby",
    "lab_lobby" to "comms_room",
    "lab_lobby" to "lab_hall",
    "conference" to "lab_cross",
    "conference" to "hall_window",
    "lab_hall" to "lab_cross",
    "lab_hall" to "lab_bench",
    "lab_cross" to "storage",
    "hall_window" to "storage",
    "lab_bench" to "lab_think",
    "lab_think" to "lab_cross",
)

private val mansionIds = mansionSpots.map { it.id }.toSet() - "inside_elevator" - "white_house_porch"
private val labIds = labSpots.map { it.id }.toSet() - "inside_elevator"

private val compass = arrayOf(
    0f to -1f,
    0.7f to -0.7f,
    1f to 0f,
    0.7f to 0.7f,
    0f to 1f,
    -0.7f to 0.7f,
    -1f to 0f,
    -0.7f to -0.7f,
)

internal fun progressChart(
    visited: Set<String>,
    here: String,
    shoreId: String?,
    seaInland: Int?,
    names: Map<String, String>,
    canEnterWater: Boolean = false,
    elevatorDown: Boolean = false,
): IslandChart {
    val indoors = when {
        here in labIds || (here == "inside_elevator" && elevatorDown) -> labSpots to labLinks
        here in mansionIds || here == "inside_elevator" -> mansionSpots to mansionLinks
        else -> coastSpots to coastLinks
    }
    return drawChart(
        spots = indoors.first,
        connections = indoors.second,
        visited = visited,
        here = here,
        shoreId = if (indoors.first === coastSpots) shoreId else null,
        seaInland = if (indoors.first === coastSpots) seaInland else null,
        names = names,
        canEnterWater = canEnterWater,
    )
}

internal fun coastChart(
    visited: Set<String>,
    here: String,
    shoreId: String?,
    seaInland: Int?,
    names: Map<String, String>,
    canEnterWater: Boolean = false,
): IslandChart = drawChart(coastSpots, coastLinks, visited, here, shoreId, seaInland, names, canEnterWater)

private fun drawChart(
    spots: List<ChartSpot>,
    connections: List<Pair<String, String>>,
    visited: Set<String>,
    here: String,
    shoreId: String?,
    seaInland: Int?,
    names: Map<String, String>,
    canEnterWater: Boolean,
): IslandChart {
    val byId = spots.associateBy { it.id }
    val marks = mutableListOf<ChartMark>()
    spots.forEach { spot ->
        if (spot.id in visited) {
            marks += ChartMark(spot.id, spot.label, spot.x, spot.y, here = spot.id == here)
        }
    }
    if (here == "ocean") {
        val shore = byId[shoreId]
        val inland = seaInland
        if (shore != null && inland != null && inland in compass.indices) {
            val (dx, dy) = compass[(inland + 4) % 8]
            marks += ChartMark(
                id = "ocean",
                label = "Mar",
                x = (shore.x + dx * 0.16f).coerceIn(0.04f, 0.96f),
                y = (shore.y + dy * 0.16f).coerceIn(0.04f, 0.96f),
                here = true,
            )
        }
    }
    val index = marks.mapIndexed { index, mark -> mark.id to index }.toMap()
    val links = connections.mapNotNull { (from, to) ->
        val left = index[from]
        val right = index[to]
        if (left != null && right != null) left to right else null
    }
    val drawn = marks.map { it.id }.toSet()
    val elsewhere = names
        .filter { (id, _) -> id in visited && id !in drawn }
        .map { (id, name) ->
            ChartNote(id, name, canTravel = id != "ocean" || canEnterWater)
        }
    return IslandChart(marks, links, elsewhere)
}
