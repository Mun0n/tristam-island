package com.tristamisland.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class IslandLeafTest {
    @Test
    fun theGuideIsPageSixOfTheLogbook() {
        assertEquals("Cómo se juega", guideLeaf.title)
        assertEquals("pág. vi", guideLeaf.folio)
        assertEquals(LOGBOOK_NAME, "Cuaderno de derrota")
        assertEquals(
            listOf(
                "Las decisiones están en la escena. Elige una y el juego responde siempre igual.",
                "Puedes llevar tres cosas en las manos. La partida se guarda sola al terminar cada acción.",
                "No hay conexión y no hay texto libre. Si una acción no aparece, ahora mismo no está a tu alcance.",
            ),
            guideLeaf.paragraphs,
        )
    }

    @Test
    fun theCreditsArePageSevenOfTheLogbook() {
        assertEquals("Créditos", creditsLeaf.title)
        assertEquals("pág. vii", creditsLeaf.folio)
        assertEquals(
            listOf(
                "Tristam Island es un juego de Hugo Labrande, publicado como CC0 1.0.",
                "Esta adaptación es una obra aparte: texto en español, decisiones en pantalla y partida local. " +
                    "No incluye la portada original ni el código de PunyInform.",
                "El capítulo jugable es la costa baja. El resto de la isla queda fuera de esta versión.",
                "En el menú suena Island on the Horizon. En la costa baja, La costa baja.",
            ),
            creditsLeaf.paragraphs,
        )
    }
}
