package com.tristamisland.domain.island

internal data class RadioResult(
    val state: IslandState,
    val response: String,
    val score: Int = 0,
    val journalId: String? = null,
    val flag: String? = null,
)

internal fun tuneRadio(state: IslandState, direction: String): RadioResult? {
    val step = when (direction) {
        "left" -> -IslandRules.RADIO_LEFT
        "right" -> IslandRules.RADIO_RIGHT
        else -> return null
    }
    var freq = state.radioFreq + step
    val coast = freq == IslandRules.RADIO_COAST && state.flags["freq_reached"] != true
    val response = when {
        step < 0 && freq < IslandRules.RADIO_LOW -> {
            freq = IslandRules.RADIO_LOW
            "Giras el mando a la izquierda, pero se atasca. Es el final de la banda: ${megacycles(freq)}."
        }
        step > 0 && freq > IslandRules.RADIO_HIGH -> {
            freq = IslandRules.RADIO_HIGH
            "Giras el mando a la derecha, pero se atasca. Es el final de la banda: ${megacycles(freq)}."
        }
        else -> "El mando está oxidado y ya no es preciso. Caes en ${megacycles(freq)}."
    }
    val spoken = if (coast) "$response Es la de los guardacostas." else response
    return RadioResult(
        state = state.copy(radioFreq = freq),
        response = spoken,
        score = if (coast) 5 else 0,
        journalId = if (coast) "radio_frequency" else null,
        flag = if (coast) "freq_reached" else null,
    )
}

internal fun callRadio(state: IslandState): RadioResult {
    val freq = megacycles(state.radioFreq)
    if (state.flags["headphones_on"] != true) {
        return RadioResult(
            state = state,
            response = "Deberías ponerte los auriculares antes, para oír si responden.",
        )
    }
    if (state.flags["cable_fixed"] != true) {
        return RadioResult(
            state = state,
            response = "Aprietas el botón del micrófono y pides ayuda en $freq, pero nadie responde. En los auriculares no se oye nada.",
        )
    }
    if (state.radioFreq != IslandRules.RADIO_COAST || state.flags["rescued"] == true) {
        return RadioResult(
            state = state,
            response = "Aprietas el botón del micrófono y pides ayuda en $freq, pero nadie responde. Solo hay estática.",
        )
    }
    val ending = if (state.flags["tristam_seen"] == true) RESCUE_TRISTAM else RESCUE_NEAR
    return RadioResult(
        state = state,
        response = ending,
        score = 5,
        journalId = "rescue",
        flag = "rescued",
    )
}

internal fun megacycles(freq: Int): String = "12.$freq Mc"

private const val RESCUE_NEAR =
    "Aprietas el botón del micrófono y pides ayuda una y otra vez en 12.4035 Mc. Solo hay estática. Estás a punto de quitarte los auriculares cuando responden: guardacostas de Namibia, SOS, ¿me oyes? Sí. Hablas con alguien por primera vez en mucho tiempo. Unas horas después confirman tu posición y te piden que esperes. Pasan unos días. Cuando ves el helicóptero, gritas. Aterriza en el campo, delante de la casa blanca. Baja un oficial. Soy el coronel Bradley, y he venido a llevarle a casa. ¿Listo? Nunca he estado tan listo. Al subir oyes un graznido: el albatros te mira. Adiós, amigo. El pájaro no contesta, pero imaginas que te agradece el pez. El helicóptero gana altura. Abajo, la isla de al lado, pequeña y quemada, se queda en un punto en medio del Atlántico. El secreto se viene contigo."

private const val RESCUE_TRISTAM =
    "Aprietas el botón del micrófono y pides ayuda una y otra vez en 12.4035 Mc. Solo hay estática. Estás a punto de quitarte los auriculares cuando responden: guardacostas de Namibia, SOS, ¿me oyes? Sí. Hablas con alguien por primera vez en mucho tiempo. Unas horas después confirman tu posición y te piden que esperes. Pasan unos días. Cuando ves el helicóptero, gritas. Aterriza en el campo, delante de la casa blanca. Baja un oficial. Soy el coronel Bradley, y he venido a llevarle a casa. ¿Listo? Nunca he estado tan listo. Al subir oyes un graznido: el albatros te mira. Adiós, amigo. El pájaro no contesta, pero imaginas que te agradece el pez. El helicóptero gana altura. Abajo, Tristam, pequeña y quemada, se queda en un punto en medio del Atlántico. El secreto se viene contigo."
