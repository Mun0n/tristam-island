package com.tristamisland.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class GothicThemeTest {
    @Test
    fun thePageIsNightAndTheChoiceRowIsASeparateSlab() {
        val scheme = gothicColorScheme()
        assertEquals(GothicColors.page, scheme.background)
        assertEquals(GothicColors.page, scheme.surface)
        assertEquals(GothicColors.header, scheme.surfaceContainerLowest)
        assertEquals(GothicColors.chronicle, scheme.surfaceContainerLow)
        assertEquals(GothicColors.choice, scheme.surfaceContainer)
        assertEquals(GothicColors.pressed, scheme.surfaceContainerHigh)
        assertEquals(GothicColors.life, scheme.secondaryContainer)
        assertEquals(GothicColors.primary, scheme.primary)
        assertEquals(GothicColors.onPrimary, scheme.onPrimary)
        assertEquals(GothicColors.outline, scheme.outline)
        assertEquals(Color(0xFF13131B), GothicColors.page)
        assertEquals(Color(0xFF1F1F28), GothicColors.choice)
        assertEquals(Color(0xFF8B1E2D), GothicColors.life)
        assertEquals(Color(0xFFE9C176), GothicColors.primary)
    }

    @Test
    fun cardsUseEightDpAndChipsUseFour() {
        val shapes = gothicShapes()
        assertEquals(RoundedCornerShape(4.dp), shapes.extraSmall)
        assertEquals(RoundedCornerShape(4.dp), shapes.small)
        assertEquals(RoundedCornerShape(8.dp), shapes.medium)
        assertEquals(RoundedCornerShape(8.dp), shapes.large)
        assertEquals(RoundedCornerShape(8.dp), shapes.extraLarge)
    }

    @Test
    fun chronicleIsNewsreaderAndLabelsAreGrotesk() {
        val type = gothicTypography()
        assertEquals(17.sp, type.bodyLarge.fontSize)
        assertEquals(26.sp, type.bodyLarge.lineHeight)
        assertEquals(FontWeight.Normal, type.bodyLarge.fontWeight)
        assertEquals(34.sp, type.displayLarge.fontSize)
        assertEquals(FontWeight.Bold, type.displayLarge.fontWeight)
        assertEquals(0.02.em, type.displayLarge.letterSpacing)
        assertEquals(13.sp, type.labelLarge.fontSize)
        assertEquals(FontWeight.SemiBold, type.labelLarge.fontWeight)
        assertEquals(0.08.em, type.labelLarge.letterSpacing)
        assertEquals(9.sp, type.labelSmall.fontSize)
        assertEquals(type.bodyLarge.fontFamily, type.bodyMedium.fontFamily)
        assertEquals(type.displayLarge.fontFamily, type.headlineLarge.fontFamily)
        assertEquals(type.labelLarge.fontFamily, type.labelSmall.fontFamily)
    }
}
