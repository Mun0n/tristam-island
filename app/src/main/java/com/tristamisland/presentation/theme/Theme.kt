package com.tristamisland.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.tristamisland.R

/**
 * The page is the YAML surface. The choice row is surface-container.
 * Life is secondary-container. The hairline is the approved outline.
 */
internal object GothicColors {
    val page: Color = Color(0xFF13131B)
    val header: Color = Color(0xFF0D0D16)
    val chronicle: Color = Color(0xFF1B1B24)
    val choice: Color = Color(0xFF1F1F28)
    val pressed: Color = Color(0xFF292933)
    val highest: Color = Color(0xFF34343E)
    val bright: Color = Color(0xFF393842)
    val onSurface: Color = Color(0xFFE4E1EE)
    val onSurfaceVariant: Color = Color(0xFFD1C5B4)
    val primary: Color = Color(0xFFE9C176)
    val onPrimary: Color = Color(0xFF412D00)
    val primaryContainer: Color = Color(0xFFC5A059)
    val onPrimaryContainer: Color = Color(0xFF4E3700)
    val inversePrimary: Color = Color(0xFF775A19)
    val secondary: Color = Color(0xFFFFB3B4)
    val onSecondary: Color = Color(0xFF680017)
    val life: Color = Color(0xFF8B1E2D)
    val onLife: Color = Color(0xFFFF9DA1)
    val tertiary: Color = Color(0xFFFFB872)
    val onTertiary: Color = Color(0xFF4A2800)
    val tertiaryContainer: Color = Color(0xFFDA9753)
    val onTertiaryContainer: Color = Color(0xFF593100)
    val outline: Color = Color(0xFF4E4639)
    val inverseSurface: Color = Color(0xFFE4E1EE)
    val inverseOnSurface: Color = Color(0xFF302F39)
    val error: Color = Color(0xFFFFB4AB)
    val onError: Color = Color(0xFF690005)
    val errorContainer: Color = Color(0xFF93000A)
    val onErrorContainer: Color = Color(0xFFFFDAD6)
}

private val Playfair = FontFamily(
    Font(
        R.font.playfair_display,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.playfair_display,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val Newsreader = FontFamily(
    Font(
        R.font.newsreader,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("opsz", 17f),
        ),
    ),
)

private val SpaceGrotesk = FontFamily(
    Font(
        R.font.space_grotesk,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.space_grotesk,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

internal fun gothicColorScheme() = darkColorScheme(
    primary = GothicColors.primary,
    onPrimary = GothicColors.onPrimary,
    primaryContainer = GothicColors.primaryContainer,
    onPrimaryContainer = GothicColors.onPrimaryContainer,
    inversePrimary = GothicColors.inversePrimary,
    secondary = GothicColors.secondary,
    onSecondary = GothicColors.onSecondary,
    secondaryContainer = GothicColors.life,
    onSecondaryContainer = GothicColors.onLife,
    tertiary = GothicColors.tertiary,
    onTertiary = GothicColors.onTertiary,
    tertiaryContainer = GothicColors.tertiaryContainer,
    onTertiaryContainer = GothicColors.onTertiaryContainer,
    background = GothicColors.page,
    onBackground = GothicColors.onSurface,
    surface = GothicColors.page,
    onSurface = GothicColors.onSurface,
    surfaceVariant = GothicColors.highest,
    onSurfaceVariant = GothicColors.onSurfaceVariant,
    surfaceTint = GothicColors.primary,
    inverseSurface = GothicColors.inverseSurface,
    inverseOnSurface = GothicColors.inverseOnSurface,
    error = GothicColors.error,
    onError = GothicColors.onError,
    errorContainer = GothicColors.errorContainer,
    onErrorContainer = GothicColors.onErrorContainer,
    outline = GothicColors.outline,
    outlineVariant = GothicColors.outline,
    scrim = Color.Black,
    surfaceBright = GothicColors.bright,
    surfaceDim = GothicColors.page,
    surfaceContainerLowest = GothicColors.header,
    surfaceContainerLow = GothicColors.chronicle,
    surfaceContainer = GothicColors.choice,
    surfaceContainerHigh = GothicColors.pressed,
    surfaceContainerHighest = GothicColors.highest,
)

internal fun gothicShapes() = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

internal fun gothicTypography() = Typography(
    displayLarge = playfair(34.sp, 42.sp, FontWeight.Bold, 0.02.em),
    displayMedium = playfair(26.sp, 34.sp, FontWeight.Bold, 0.015.em),
    displaySmall = playfair(26.sp, 34.sp, FontWeight.Bold, 0.015.em),
    headlineLarge = playfair(22.sp, 28.sp, FontWeight.SemiBold, 0.em),
    headlineMedium = playfair(22.sp, 28.sp, FontWeight.SemiBold, 0.em),
    headlineSmall = playfair(18.sp, 24.sp, FontWeight.SemiBold, 0.em),
    titleLarge = playfair(22.sp, 28.sp, FontWeight.SemiBold, 0.em),
    titleMedium = playfair(18.sp, 24.sp, FontWeight.SemiBold, 0.em),
    titleSmall = playfair(18.sp, 24.sp, FontWeight.SemiBold, 0.em),
    bodyLarge = newsreader(17.sp, 26.sp),
    bodyMedium = newsreader(15.sp, 22.sp),
    bodySmall = newsreader(13.sp, 18.sp),
    labelLarge = grotesk(13.sp, 18.sp, FontWeight.SemiBold, 0.08.em),
    labelMedium = grotesk(11.sp, 14.sp, FontWeight.Medium, 0.06.em),
    labelSmall = grotesk(9.sp, 12.sp, FontWeight.Medium, 0.05.em),
)

private fun playfair(size: TextUnit, line: TextUnit, weight: FontWeight, tracking: TextUnit) = TextStyle(
    fontFamily = Playfair,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
    letterSpacing = tracking,
)

private fun newsreader(size: TextUnit, line: TextUnit) = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = size,
    lineHeight = line,
)

private fun grotesk(size: TextUnit, line: TextUnit, weight: FontWeight, tracking: TextUnit) = TextStyle(
    fontFamily = SpaceGrotesk,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
    letterSpacing = tracking,
)

@Composable
fun TristamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = gothicColorScheme(),
        shapes = gothicShapes(),
        typography = gothicTypography(),
        content = content,
    )
}
