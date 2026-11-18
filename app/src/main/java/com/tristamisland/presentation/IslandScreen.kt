package com.tristamisland.presentation

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tristamisland.TristamApp
import com.tristamisland.R
import com.tristamisland.presentation.theme.GothicColors
import java.util.Locale

@Composable
fun IslandApp(model: IslandViewModel) {
    val ui by model.ui.collectAsStateWithLifecycle()
    val app = LocalContext.current.applicationContext as TristamApp
    var musicOn by remember { mutableStateOf(app.musicPreference.enabled()) }
    LaunchedEffect(ui.mode, ui.page, ui.play.musicRegion, musicOn) {
        val cue = musicCue(ui.mode, ui.page, ui.play.musicRegion, musicOn)
        val track = cue.track
        app.backgroundMusic.render(
            res = track?.let(::trackResource),
            volume = cue.volume,
            label = track?.label.orEmpty(),
        )
    }
    val onMusic = {
        musicOn = !musicOn
        app.musicPreference.setEnabled(musicOn)
    }
    when (ui.mode) {
        IslandMode.MENU -> IslandMenu(ui, model, musicOn, onMusic)
        IslandMode.GUIDE -> IslandTextPage(
            leaf = guideLeaf,
            onBack = { model.open(IslandMode.MENU) },
        )
        IslandMode.CREDITS -> IslandTextPage(
            leaf = creditsLeaf,
            onBack = { model.open(IslandMode.MENU) },
        )
        IslandMode.PLAY -> IslandPlayScreen(ui, model, musicOn, onMusic)
    }
    if (ui.confirmNew) {
        NewGameDialog(
            onStart = model::startNewGame,
            onDismiss = model::dismissNewGame,
        )
    }
}

@Composable
private fun NewGameDialog(onStart: () -> Unit, onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .shadow(18.dp, shape, clip = false)
                    .clip(shape)
                    .background(Expedition.card)
                    .pointerInput(Unit) { detectTapGestures { } }
                    .semantics { paneTitle = "Nueva partida" },
            ) {
                Column(
                    modifier = Modifier.padding(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Aviso de expedición".uppercase(SPANISH),
                        color = Expedition.cardKicker,
                        fontFamily = ExpeditionSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.18.em,
                    )
                    Text(
                        text = "Nueva partida",
                        color = Expedition.cardInk,
                        fontFamily = ExpeditionSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        letterSpacing = (-0.01).em,
                        lineHeight = 32.sp,
                    )
                    Text(
                        text = "La partida guardada se sustituye. No hay copia.",
                        color = Expedition.cardBody,
                        fontFamily = ExpeditionSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
                HorizontalDivider(color = Expedition.cardLine, thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    DialogAction(
                        label = "Cancelar",
                        color = Expedition.cardCancel,
                        onClick = onDismiss,
                    )
                    DialogAction(
                        label = "Empezar",
                        color = Expedition.cardConfirm,
                        onClick = onStart,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DialogAction(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = color),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(SPANISH),
            color = color,
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.14.em,
        )
    }
}

@Composable
private fun IslandMenu(
    ui: IslandUi,
    model: IslandViewModel,
    musicOn: Boolean,
    onMusic: () -> Unit,
) {
    val resumeInRows = ui.hasSave || !ui.ready
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Expedition.paper),
    ) {
        val screenHeight = maxHeight
        val hero = (screenHeight * 0.38f).coerceIn(240.dp, 360.dp)
        val bodyMin = (screenHeight - hero).coerceAtLeast(0.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            ExpeditionHero(Modifier.fillMaxWidth().height(hero))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = bodyMin)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (ui.ready && ui.hasSave) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CurrentExpedition(ui.play)
                            ExpeditionButton(
                                label = "Continuar",
                                enabled = !ui.busy,
                                onClick = model::continueGame,
                            )
                        }
                    } else if (ui.ready) {
                        ExpeditionButton(
                            label = "Nueva partida",
                            enabled = !ui.busy,
                            onClick = model::requestNewGame,
                        )
                    }
                    Column {
                        HorizontalDivider(color = Expedition.borderLine, thickness = 1.dp)
                        if (resumeInRows) {
                            ExpeditionRow(
                                label = "Nueva partida",
                                icon = R.drawable.ic_expedition_compass,
                                enabled = ui.ready && !ui.busy,
                                onClick = model::requestNewGame,
                            )
                        }
                        ExpeditionRow(
                            label = "Cómo se juega",
                            icon = R.drawable.ic_expedition_book,
                            enabled = ui.ready && !ui.busy,
                            onClick = { model.open(IslandMode.GUIDE) },
                        )
                        ExpeditionRow(
                            label = "Créditos",
                            icon = R.drawable.ic_expedition_journal,
                            enabled = ui.ready && !ui.busy,
                            onClick = { model.open(IslandMode.CREDITS) },
                        )
                        MusicPreferenceRow(musicOn, onMusic)
                    }
                }
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    HorizontalDivider(color = Expedition.borderLine.copy(alpha = 0.7f), thickness = 1.dp)
                    Text(
                        text = "Una obra de Hugo Labrande",
                        color = Expedition.slateMuted,
                        fontFamily = ExpeditionSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        letterSpacing = 0.04.em,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    )
                    Text(
                        text = "Ficción interactiva · Edición expedición".uppercase(SPANISH),
                        color = Expedition.slateInactive,
                        fontFamily = ExpeditionSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 0.18.em,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpeditionHero(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.menu_storm),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.40f to Expedition.paper.copy(alpha = 0.30f),
                        0.75f to Expedition.paper.copy(alpha = 0.85f),
                        1f to Expedition.paper,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "40°19' S  ·  9°53' W",
                color = Expedition.slateMuted.copy(alpha = 0.90f),
                fontFamily = ExpeditionSans,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.22.em,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Una aventura en el Atlántico Sur".uppercase(SPANISH),
                    color = Expedition.mist,
                    fontFamily = ExpeditionSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.24.em,
                    lineHeight = 12.sp,
                )
                Text(
                    text = "Tristam Island",
                    color = Expedition.parchment,
                    fontFamily = ExpeditionSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 38.sp,
                    letterSpacing = (-0.02).em,
                    lineHeight = 41.sp,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Expedition.borderLine),
        )
    }
}

@Composable
private fun CurrentExpedition(play: IslandPlay) {
    val place = play.placeName
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SaveDot()
            Text(
                text = "Partida actual".uppercase(SPANISH),
                color = Expedition.coral,
                fontFamily = ExpeditionSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.18.em,
            )
        }
        if (play.actTitle.isNotBlank()) {
            Text(
                text = play.actTitle,
                color = Expedition.parchment,
                fontFamily = ExpeditionSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                letterSpacing = (-0.01).em,
                lineHeight = 32.sp,
            )
        }
        Text(
            text = if (place.isBlank()) "Guardado local" else "$place  ·  Guardado local",
            color = Expedition.slateMuted,
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            letterSpacing = 0.02.em,
        )
    }
}

@Composable
private fun SaveDot() {
    val transition = rememberInfiniteTransition(label = "save")
    val animated by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "save-dot",
    )
    val alpha = if (reducedMotion()) 1f else animated
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(Expedition.coral.copy(alpha = alpha)),
    )
}

@Composable
private fun ExpeditionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(if (enabled) Expedition.atlanticBtn else Expedition.atlanticBtn.copy(alpha = 0.45f))
            .border(1.dp, Expedition.atlanticTeal.copy(alpha = 0.30f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Expedition.parchment.copy(alpha = if (enabled) 1f else 0.6f),
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            letterSpacing = 0.02.em,
        )
        Icon(
            painter = painterResource(R.drawable.ic_expedition_arrow),
            contentDescription = null,
            tint = Expedition.parchment.copy(alpha = if (enabled) 0.9f else 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MusicPreferenceRow(enabled: Boolean, onClick: () -> Unit) {
    val value = if (enabled) "Sí" else "No"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = if (enabled) "Música activada" else "Música en silencio"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Música",
            color = Expedition.parchment.copy(alpha = 0.9f),
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            letterSpacing = (-0.01).em,
            modifier = Modifier.padding(start = 32.dp),
        )
        Text(
            text = value,
            color = Expedition.slateMuted,
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.08.em,
        )
    }
}

@Composable
private fun MusicControl(enabled: Boolean, onToggle: () -> Unit) {
    Text(
        text = if (enabled) "Música" else "Silencio",
        color = GothicColors.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .clickable(role = Role.Button, onClick = onToggle)
            .semantics {
                contentDescription = if (enabled) "Silenciar la música" else "Activar la música"
            },
    )
}

@Composable
private fun ExpeditionRow(label: String, icon: Int, enabled: Boolean, onClick: () -> Unit) {
    val labelColor = if (enabled) Expedition.parchment.copy(alpha = 0.9f) else Expedition.slateInactive
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (enabled) Expedition.slateMuted else Expedition.slateInactive,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                color = labelColor,
                fontFamily = ExpeditionSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                letterSpacing = (-0.01).em,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_expedition_chevron),
            contentDescription = null,
            tint = Expedition.chevron.copy(alpha = if (enabled) 0.7f else 0.35f),
            modifier = Modifier.size(16.dp),
        )
    }
    HorizontalDivider(color = Expedition.borderLine, thickness = 1.dp)
}

@Composable
private fun IslandTextPage(leaf: IslandLeaf, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Expedition.storm),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 412.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = 12.dp, bottom = 20.dp),
        ) {
            LeafHeader(onBack)
            LeafBody(leaf, Modifier.weight(1f))
            LeafFooter(leaf.folio)
        }
    }
}

@Composable
private fun LeafHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onBack),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_expedition_back),
                contentDescription = null,
                tint = Expedition.navActive,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "MENÚ",
                color = Expedition.navActive,
                fontFamily = ExpeditionSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.22.em,
            )
        }
        Text(
            text = "Tristam Island",
            color = Expedition.driftwood.copy(alpha = 0.6f),
            fontFamily = LeafItalic,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.05.em,
        )
    }
}

@Composable
private fun LeafBody(leaf: IslandLeaf, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .heightIn(min = maxHeight)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = leaf.title,
                color = Expedition.parchment,
                fontFamily = LeafTitle,
                fontWeight = FontWeight.Normal,
                fontSize = 38.sp,
                letterSpacing = (-0.025).em,
                lineHeight = 47.sp,
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(32.dp)
                    .height(1.dp)
                    .background(Expedition.rule.copy(alpha = 0.4f)),
            )
            Column(
                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                leaf.paragraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        color = Expedition.ivory,
                        fontFamily = ExpeditionSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        lineHeight = 33.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeafFooter(folio: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.04f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = LOGBOOK_NAME.uppercase(SPANISH),
                color = Expedition.driftwood,
                fontFamily = ExpeditionSans,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 0.2.em,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
            Text(
                text = folio,
                color = Expedition.driftwood,
                fontFamily = LeafItalic,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun IslandPlayScreen(
    ui: IslandUi,
    model: IslandViewModel,
    musicOn: Boolean,
    onMusic: () -> Unit,
) {
    val actCard = ui.play.actCard
    if (actCard != null) {
        ActTitle(
            label = actCard,
            enabled = !ui.busy,
            onContinue = model::acknowledgeAct,
            onMenu = { model.open(IslandMode.MENU) },
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Expedition.paper)
            .navigationBarsPadding(),
    ) {
        when (ui.page) {
            IslandPage.SCENE -> ScenePage(ui, model, musicOn, onMusic, Modifier.weight(1f))
            IslandPage.JOURNAL -> JournalPage(
                notes = ui.play.journal,
                modifier = Modifier.weight(1f),
                onMenu = { model.open(IslandMode.MENU) },
            )
            IslandPage.INVENTORY -> InventoryPage(ui.play, Modifier.weight(1f)) { model.open(IslandMode.MENU) }
            IslandPage.STATUS -> StatusPage(
                play = ui.play,
                modifier = Modifier.weight(1f),
                onMenu = { model.open(IslandMode.MENU) },
                onTravel = model::travelTo,
            )
        }
        PlayBar(ui.page, model::openPage)
    }
}

@Composable
private fun ActTitle(
    label: String,
    enabled: Boolean,
    onContinue: () -> Unit,
    onMenu: () -> Unit,
) {
    BackHandler(onBack = onMenu)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Expedition.storm)
            .clickable(enabled = enabled, role = Role.Button, onClick = onContinue)
            .semantics { contentDescription = "$label. Toca para continuar" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Expedition.ivory,
            fontFamily = ExpeditionSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 44.sp,
            letterSpacing = 0.08.em,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = "Toca para continuar",
            color = Expedition.driftwood,
            fontFamily = ExpeditionSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .clearAndSetSemantics {},
        )
    }
}

@Composable
private fun ScenePage(
    ui: IslandUi,
    model: IslandViewModel,
    musicOn: Boolean,
    onMusic: () -> Unit,
    modifier: Modifier,
) {
    val play = ui.play
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GothicColors.header)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Menú",
                    color = GothicColors.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .clickable { model.open(IslandMode.MENU) },
                )
                MusicControl(musicOn, onMusic)
            }
            if (play.actTitle.isNotBlank()) {
                Text(
                    text = play.actTitle.uppercase(),
                    color = GothicColors.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = play.heading,
                color = GothicColors.onSurface,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        val caption = if (play.inPrologue) "Atlántico sur" else play.heading
        val art = islandArtRes(play.inPrologue, play.prologueIndex, play.locationId)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(GothicColors.chronicle),
            contentAlignment = Alignment.BottomStart,
        ) {
            if (art != null) {
                Image(
                    painter = painterResource(art),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Color(0xCC0D0D16)),
                        ),
                    ),
            )
            Text(
                text = caption,
                color = GothicColors.onSurface,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = play.passage, color = GothicColors.onSurface, style = MaterialTheme.typography.bodyLarge)
            if (!play.response.isNullOrBlank()) {
                Text(text = play.response, color = GothicColors.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
            ui.notice?.let { notice ->
                Text(text = notice, color = GothicColors.tertiary, style = MaterialTheme.typography.bodyMedium)
            }
            if (play.inPrologue) {
                ChoiceRow("Seguir", enabled = !ui.busy, onClick = model::continuePrologue)
            } else {
                play.actions.forEach { action ->
                    ChoiceRow(action.label, enabled = !ui.busy) { model.choose(action.id) }
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (enabled) GothicColors.onSurface else GothicColors.outline,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GothicColors.choice)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Composable
private fun JournalPage(notes: List<IslandNote>, modifier: Modifier, onMenu: () -> Unit) {
    Column(
        modifier = modifier
            .background(Expedition.paper)
            .statusBarsPadding(),
    ) {
        JournalHeader(notes.size, onMenu)
        HorizontalDivider(color = Expedition.headerLine, thickness = 1.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 32.dp),
        ) {
            Text(
                text = "Lo que has observado en la isla.",
                color = Expedition.intro,
                fontFamily = ExpeditionSerif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            )
            Spacer(Modifier.height(32.dp))
            if (notes.isEmpty()) {
                Text(
                    text = "Todavía no has anotado nada.",
                    color = Expedition.slateMuted,
                    fontFamily = ExpeditionSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                )
            } else {
                JournalTimeline(notes)
                JournalEnd()
            }
        }
    }
}

@Composable
private fun JournalHeader(count: Int, onMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .height(48.dp)
                    .semantics { contentDescription = "Menú" }
                    .clickable(role = Role.Button, onClick = onMenu),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Box(
                    Modifier
                        .width(16.dp)
                        .height(1.5.dp)
                        .background(Expedition.menuMark, CircleShape),
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(12.dp)
                        .height(1.5.dp)
                        .background(Expedition.menuMark, CircleShape),
                )
            }
            Text(
                text = "Diario",
                color = Expedition.parchment,
                fontFamily = ExpeditionSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                letterSpacing = (-0.02).em,
            )
        }
        Text(
            text = noteCount(count),
            color = Expedition.noteCount,
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.08.em,
        )
    }
}

@Composable
private fun JournalTimeline(notes: List<IslandNote>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 7.5.dp.toPx()
                drawLine(
                    color = Expedition.timeline,
                    start = Offset(x, 12.dp.toPx()),
                    end = Offset(x, size.height - 24.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            .padding(start = 24.dp),
    ) {
        notes.forEachIndexed { index, note ->
            JournalEntry(
                note = note,
                number = index + 1,
                accent = index == 0,
                last = index == notes.lastIndex,
            )
        }
    }
}

@Composable
private fun JournalEntry(note: IslandNote, number: Int, accent: Boolean, last: Boolean) {
    val node = if (accent) Expedition.coral else Expedition.nodeQuiet
    val dot = if (accent) Expedition.coral else Expedition.nodeDot
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .offset(x = (-24).dp)
                .padding(top = 6.dp)
                .size(15.dp)
                .background(Expedition.paper, CircleShape)
                .border(2.dp, node, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(dot, CircleShape),
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "%02d".format(SPANISH, number),
                    color = Expedition.coral,
                    fontFamily = ExpeditionSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = (-0.02).em,
                )
                if (note.place.isNotBlank()) {
                    Text(
                        text = note.place.uppercase(SPANISH),
                        color = Expedition.placeLabel,
                        fontFamily = ExpeditionSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.14.em,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = note.text,
                    color = Expedition.parchment,
                    fontFamily = ExpeditionSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    modifier = Modifier.weight(1f),
                )
                if (note.id == FLOWER_NOTE) {
                    CoastalFlowerStudy()
                }
            }
            if (last) {
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Expedition.borderLine, thickness = 1.dp)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CoastalFlowerStudy() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Expedition.plate)
            .border(1.dp, Expedition.plateLine, RoundedCornerShape(8.dp))
            .padding(2.dp)
            .semantics { contentDescription = "Estudio botánico de una flor de la costa" },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF3EEE4)),
        ) {
            val stem = Color(0xFF5C7044)
            val leaf = Color(0xFF7D8C58)
            val petal = Color(0xFFE06D48)
            val petalDeep = Color(0xFFC85A38)
            val heart = Color(0xFFE7B84A)
            val root = Color(0xFF8D7B62)
            val w = size.width
            val h = size.height
            val x = w * 0.50f
            val bloom = Offset(x, h * 0.33f)
            val hair = 1.dp.toPx()
            val stalk = 1.6.dp.toPx()
            drawLine(root, Offset(x, h * 0.78f), Offset(x - w * 0.12f, h * 0.92f), hair, StrokeCap.Round)
            drawLine(root, Offset(x, h * 0.78f), Offset(x + w * 0.04f, h * 0.94f), hair, StrokeCap.Round)
            drawLine(root, Offset(x, h * 0.80f), Offset(x + w * 0.14f, h * 0.90f), hair, StrokeCap.Round)
            drawLine(stem, Offset(x, h * 0.40f), Offset(x, h * 0.80f), stalk, StrokeCap.Round)
            rotate(-36f, Offset(x, h * 0.58f)) {
                drawOval(leaf, Offset(x, h * 0.54f), Size(w * 0.26f, h * 0.09f))
            }
            rotate(28f, Offset(x, h * 0.68f)) {
                drawOval(leaf, Offset(x - w * 0.24f, h * 0.64f), Size(w * 0.24f, h * 0.08f))
            }
            val orbit = w * 0.10f
            val petalRadius = w * 0.095f
            repeat(5) { index ->
                val angle = Math.toRadians((index * 72.0) - 90.0)
                drawCircle(
                    color = if (index % 2 == 0) petal else petalDeep,
                    radius = petalRadius,
                    center = Offset(
                        bloom.x + cos(angle).toFloat() * orbit,
                        bloom.y + sin(angle).toFloat() * orbit,
                    ),
                )
            }
            drawCircle(heart, w * 0.042f, bloom)
            drawCircle(Color(0xFFF6E7B2), w * 0.018f, bloom)
        }
    }
}

@Composable
private fun JournalEnd() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(24.dp)
                .height(1.dp)
                .background(Expedition.headerLine),
        )
        Text(
            text = "Fin de anotaciones actuales".uppercase(SPANISH),
            color = Expedition.endMark,
            fontFamily = ExpeditionSans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.08.em,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 8.dp),
        )
        Box(
            Modifier
                .width(24.dp)
                .height(1.dp)
                .background(Expedition.headerLine),
        )
    }
}

@Composable
private fun InventoryPage(play: IslandPlay, modifier: Modifier, onMenu: () -> Unit) {
    Column(modifier = modifier.background(GothicColors.page).statusBarsPadding()) {
        PageTitle("Inventario", onMenu)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Manos: ${play.carried.size} de 3",
                color = GothicColors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            if (play.carried.isEmpty()) {
                Text(
                    text = "No llevas nada.",
                    color = GothicColors.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                play.carried.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = item.name.replaceFirstChar { it.uppercase() },
                            color = GothicColors.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = item.description,
                            color = GothicColors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPage(
    play: IslandPlay,
    modifier: Modifier,
    onMenu: () -> Unit,
    onTravel: (String) -> Unit,
) {
    Column(modifier = modifier.background(GothicColors.page).statusBarsPadding()) {
        PageTitle("Progreso", onMenu)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CoastMap(
                chart = play.chart,
                hereName = play.placeName.ifBlank { play.heading },
                travelEnabled = !play.inPrologue,
                onTravel = onTravel,
            )
            StatusLine("Acto", play.actTitle.ifBlank { "Costa baja" })
            StatusLine("Puntos", play.score.toString())
            StatusLine("Lugares vistos", "${play.visited} de ${play.places}")
            StatusLine("En las manos", "${play.carried.size} de 3")
        }
    }
}

@Composable
private fun CoastMap(
    chart: IslandChart,
    hereName: String,
    travelEnabled: Boolean,
    onTravel: (String) -> Unit,
) {
    val known = chart.marks.joinToString(", ") { it.label }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Dónde estás",
            color = GothicColors.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = hereName.ifBlank { "Mapa" },
            color = GothicColors.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .semantics {
                    contentDescription = if (known.isBlank()) {
                        "Mapa todavía vacío"
                    } else {
                        "Mapa. $known"
                    }
                },
        ) {
            val pad = 28.dp
            val boardWidth = maxWidth - pad * 2
            val boardHeight = maxHeight - pad * 2
            Canvas(Modifier.fillMaxSize()) {
                val padPx = pad.toPx()
                val width = size.width - padPx * 2
                val height = size.height - padPx * 2
                fun at(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(
                    padPx + x * width,
                    padPx + y * height,
                )
                chart.links.forEach { (from, to) ->
                    val start = chart.marks[from]
                    val end = chart.marks[to]
                    drawLine(
                        color = Expedition.timeline,
                        start = at(start.x, start.y),
                        end = at(end.x, end.y),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
                chart.marks.forEach { mark ->
                    drawCircle(
                        color = if (mark.here) Expedition.coral else Expedition.nodeDot,
                        radius = if (mark.here) 7.dp.toPx() else 4.5.dp.toPx(),
                        center = at(mark.x, mark.y),
                    )
                }
            }
            chart.marks.forEach { mark ->
                val canGo = travelEnabled && !mark.here
                val centerX = boardWidth * mark.x
                val centerY = boardHeight * mark.y
                var halfW = 34.dp
                var halfH = 20.dp
                chart.marks.forEach { other ->
                    if (other.id == mark.id) return@forEach
                    val gapX = boardWidth * kotlin.math.abs(other.x - mark.x)
                    val gapY = boardHeight * kotlin.math.abs(other.y - mark.y)
                    if (gapY < halfH * 2 && gapX > 0.dp) halfW = minOf(halfW, gapX / 2)
                    if (gapX < halfW * 2 && gapY > 0.dp) halfH = minOf(halfH, gapY / 2)
                }
                Box(
                    modifier = Modifier
                        .offset(x = pad + centerX - halfW, y = pad + centerY - halfH)
                        .size(width = halfW * 2, height = halfH * 2)
                        .then(
                            if (canGo) {
                                Modifier.clickable(role = Role.Button) { onTravel(mark.id) }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mark.label,
                        color = if (mark.here) Expedition.coral else Expedition.mist,
                        fontFamily = ExpeditionSans,
                        fontWeight = if (mark.here) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (travelEnabled && (chart.marks.any { !it.here } || chart.elsewhere.any { it.canTravel })) {
            Text(
                text = "Pulsa un lugar para ir.",
                color = GothicColors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        chart.elsewhere.forEach { note ->
            Text(
                text = note.name,
                color = GothicColors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = if (travelEnabled && note.canTravel) {
                    Modifier.clickable(role = Role.Button) { onTravel(note.id) }
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Column {
        Text(text = label, color = GothicColors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Text(text = value, color = GothicColors.onSurface, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun PageTitle(title: String, onMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = GothicColors.onSurface, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Menú",
            color = GothicColors.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(onClick = onMenu)
                .padding(vertical = 14.dp),
        )
    }
}

@Composable
private fun PlayBar(page: IslandPage, onSelect: (IslandPage) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Expedition.paper)) {
        HorizontalDivider(color = Expedition.headerLine, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarItem(R.drawable.ic_nav_scene, "Escena", page == IslandPage.SCENE) {
                onSelect(IslandPage.SCENE)
            }
            BarItem(R.drawable.ic_nav_journal, "Diario", page == IslandPage.JOURNAL) {
                onSelect(IslandPage.JOURNAL)
            }
            BarItem(R.drawable.ic_nav_objects, "Objetos", page == IslandPage.INVENTORY) {
                onSelect(IslandPage.INVENTORY)
            }
            BarItem(R.drawable.ic_nav_progress, "Progreso", page == IslandPage.STATUS) {
                onSelect(IslandPage.STATUS)
            }
        }
    }
}

@Composable
private fun RowScope.BarItem(icon: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Expedition.navActive else Expedition.navIdle
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .semantics { this.selected = selected }
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .width(16.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(20.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontFamily = ExpeditionSans,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = (-0.01).em,
        )
    }
}

private object Expedition {
    val paper = Color(0xFF12181B)
    val storm = Color(0xFF111719)
    val ivory = Color(0xFFD8D2C6)
    val driftwood = Color(0xFF5A6468)
    val rule = Color(0xFF3A8C9E)
    val card = Color(0xFFE7DFD2)
    val cardInk = Color(0xFF1C1E16)
    val cardBody = Color(0xFF45473A)
    val cardKicker = Color(0xFF787E74)
    val cardLine = Color(0xFFD0C6B8)
    val cardCancel = Color(0xFF5A5C50)
    val cardConfirm = Color(0xFF1F5C54)
    val borderLine = Color(0xFF1E2B30)
    val headerLine = Color(0xFF233338)
    val parchment = Color(0xFFE2DDD3)
    val mist = Color(0xFFB5CCD1)
    val slateMuted = Color(0xFF8C9B9E)
    val slateInactive = Color(0xFF5F6E73)
    val chevron = Color(0xFF5C737A)
    val atlanticTeal = Color(0xFF3EA8BF)
    val atlanticBtn = Color(0xFF174B5A)
    val coral = Color(0xFFE06D48)
    val menuMark = Color(0xFF9BB0B5)
    val noteCount = Color(0xFF7C9196)
    val intro = Color(0xFF8EA2A6)
    val timeline = Color(0xFF224A56)
    val nodeQuiet = Color(0xFF2F616F)
    val nodeDot = Color(0xFF3D7D8E)
    val placeLabel = Color(0xFF53A0B3)
    val endMark = Color(0xFF465A60)
    val navIdle = Color(0xFF778B8F)
    val navActive = Color(0xFF4DB4CC)
    val plate = Color(0xFF182226)
    val plateLine = Color(0xFF26373D)
}

private fun noteCount(count: Int): String {
    val word = if (count == 1) "nota" else "notas"
    return "$count $word".uppercase(SPANISH)
}

private val LeafTitle = FontFamily(
    Font(
        R.font.newsreader,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("opsz", 36f),
        ),
    ),
)

private val LeafItalic = FontFamily(
    Font(
        R.font.newsreader,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.italic(1f),
            FontVariation.Setting("opsz", 12f),
        ),
    ),
)

private val ExpeditionSerif = FontFamily(
    Font(
        R.font.newsreader,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("opsz", 18f),
        ),
    ),
    Font(
        R.font.newsreader,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.Setting("opsz", 36f),
        ),
    ),
)

private val ExpeditionSans = FontFamily(
    Font(
        R.font.space_grotesk,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
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

private val SPANISH: Locale = Locale.forLanguageTag("es")

private const val FLOWER_NOTE: String = "found_flower"

@Composable
private fun reducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    val scale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale == 0f
}
