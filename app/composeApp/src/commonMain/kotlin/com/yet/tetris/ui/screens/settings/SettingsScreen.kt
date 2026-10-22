package com.yet.tetris.ui.screens.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.yet.tetris.domain.model.audio.MusicTheme
import com.yet.tetris.domain.model.game.RotationDirection
import com.yet.tetris.domain.model.settings.GameSettings
import com.yet.tetris.domain.model.settings.GestureSensitivity
import com.yet.tetris.domain.model.theme.PieceStyle
import com.yet.tetris.domain.model.theme.VisualTheme
import com.yet.tetris.feature.settings.PreviewSettingsComponent
import com.yet.tetris.feature.settings.SettingsComponent
import com.yet.tetris.uikit.component.chip.TetrisFilterChip
import com.yet.tetris.uikit.component.hint.HintBar
import com.yet.tetris.uikit.component.modifier.focusRing
import com.yet.tetris.uikit.theme.TetrisLiteAppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tetrislite.composeapp.generated.resources.Res
import tetrislite.composeapp.generated.resources.audio
import tetrislite.composeapp.generated.resources.back
import tetrislite.composeapp.generated.resources.music
import tetrislite.composeapp.generated.resources.music_theme
import tetrislite.composeapp.generated.resources.music_volume
import tetrislite.composeapp.generated.resources.piece_style
import tetrislite.composeapp.generated.resources.settings
import tetrislite.composeapp.generated.resources.sfx_volume
import tetrislite.composeapp.generated.resources.sound_effects
import tetrislite.composeapp.generated.resources.visual_theme
import kotlin.math.roundToInt

/**
 * Settings as a full-screen page, made for game consoles with a small landscape screen and
 * a D-pad (andr36oid's fork; it replaces the bottom sheet, whose lower sections couldn't
 * be reached on a 640x480 screen).
 *
 * Landscape and square screens get two panes: the sections on the left, the selected
 * section's controls on the right. Portrait screens get the sections as tabs on top.
 *
 * D-pad: Up/Down picks a section, Right or A goes into its controls. There Up/Down moves
 * between controls, A switches or picks, Left/Right moves a slider or between chips; Left at
 * the first chip or at a slider's minimum, or B, goes back to the sections. B on the
 * sections closes the page. Touch and mouse work as usual.
 */
@Composable
fun SettingsDialog(component: SettingsComponent) {
    Dialog(
        onDismissRequest = component::onClose,
        // B and Back are handled by the page itself: in the controls they go back to the
        // sections first
        // (Back still closes it when nothing in it has the focus, so it can't get stuck)
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SettingsPage(component)
    }
}

private enum class Section { VISUAL, PIECES, AUDIO, CONTROLS }

/** One control row on the right, described as data so the D-pad can walk through them. */
private sealed interface RowSpec {
    val key: String
    val count: Int get() = 1
}

private class SwitchSpec(
    override val key: String,
    val label: String,
    val checked: Boolean,
    val onToggle: (Boolean) -> Unit,
) : RowSpec

private class SliderSpec(
    override val key: String,
    val label: String,
    val value: Float,
    val onChange: (Float) -> Unit,
) : RowSpec

private class ChoiceSpec(
    override val key: String,
    val label: String?,
    val options: List<String>,
    val selected: Int,
    val onSelect: (Int) -> Unit,
) : RowSpec {
    override val count: Int get() = options.size
}

private const val SLIDER_STEPS = 10

@Composable
fun SettingsPage(component: SettingsComponent) {
    val model by component.model.subscribeAsState()

    var section by rememberSaveable { mutableIntStateOf(0) }
    var inControls by remember { mutableStateOf(false) }
    var row by remember { mutableIntStateOf(0) }
    var item by remember { mutableIntStateOf(0) }
    // a tapped section's controls only exist after the next composition
    var focusControls by remember { mutableStateOf(false) }
    val sectionRequesters = remember { Section.entries.map { FocusRequester() } }
    val requesters = remember { mutableMapOf<String, List<FocusRequester>>() }

    fun requestersFor(spec: RowSpec): List<FocusRequester> =
        requesters.getOrPut("${spec.key}/${spec.count}") { List(spec.count) { FocusRequester() } }

    val visualLabel = stringResource(Res.string.visual_theme)
    val pieceLabel = stringResource(Res.string.piece_style)
    val audioLabel = stringResource(Res.string.audio)
    val musicLabel = stringResource(Res.string.music)
    val musicVolumeLabel = stringResource(Res.string.music_volume)
    val musicThemeLabel = stringResource(Res.string.music_theme)
    val sfxLabel = stringResource(Res.string.sound_effects)
    val sfxVolumeLabel = stringResource(Res.string.sfx_volume)

    val sectionTitles =
        Section.entries.map {
            when (it) {
                Section.VISUAL -> visualLabel
                Section.PIECES -> pieceLabel
                Section.AUDIO -> audioLabel
                Section.CONTROLS -> "Controls"
            }
        }

    // also called from the key handler, with the current state: a key can come before the
    // composition that follows the last one
    fun rowsOf(sectionIndex: Int, settings: GameSettings): List<RowSpec> {
        val audio = settings.audioSettings
        val controls = settings.controlSettings
        return when (Section.entries[sectionIndex]) {
            Section.VISUAL ->
                listOf(
                    ChoiceSpec(
                        "visual", null, VisualTheme.entries.map(::enumLabel),
                        VisualTheme.entries.indexOf(settings.themeConfig.visualTheme),
                    ) { component.onVisualThemeChanged(VisualTheme.entries[it]) },
                )

            Section.PIECES ->
                listOf(
                    ChoiceSpec(
                        "piece", null, PieceStyle.entries.map(::enumLabel),
                        PieceStyle.entries.indexOf(settings.themeConfig.pieceStyle),
                    ) { component.onPieceStyleChanged(PieceStyle.entries[it]) },
                )

            Section.AUDIO ->
                buildList {
                    add(SwitchSpec("music", musicLabel, audio.musicEnabled, component::onMusicToggled))
                    if (audio.musicEnabled) {
                        add(SliderSpec("music_volume", musicVolumeLabel, audio.musicVolume, component::onMusicVolumeChanged))
                        add(
                            ChoiceSpec(
                                "music_theme", musicThemeLabel, MusicTheme.entries.map(::musicThemeLabel),
                                MusicTheme.entries.indexOf(audio.selectedMusicTheme),
                            ) { component.onMusicThemeChanged(MusicTheme.entries[it]) },
                        )
                    }
                    add(SwitchSpec("sfx", sfxLabel, audio.soundEffectsEnabled, component::onSoundEffectsToggled))
                    if (audio.soundEffectsEnabled) {
                        add(SliderSpec("sfx_volume", sfxVolumeLabel, audio.sfxVolume, component::onSFXVolumeChanged))
                    }
                }

            Section.CONTROLS ->
                listOf(
                    ChoiceSpec(
                        "rotation", "Primary rotation", RotationDirection.entries.map(::rotationDirectionLabel),
                        RotationDirection.entries.indexOf(controls.primaryRotateDirection),
                    ) { component.onPrimaryRotateDirectionChanged(RotationDirection.entries[it]) },
                    SwitchSpec("rotation180", "Enable 180 rotation", controls.enable180Rotation, component::on180RotationToggled),
                    ChoiceSpec(
                        "sensitivity", "Gesture sensitivity", GestureSensitivity.entries.map(::gestureSensitivityLabel),
                        GestureSensitivity.entries.indexOf(controls.gestureSensitivity),
                    ) { component.onGestureSensitivityChanged(GestureSensitivity.entries[it]) },
                )
        }
    }

    val rows = rowsOf(section, model.settings)

    fun focusSections() {
        inControls = false
        runCatching { sectionRequesters[section].requestFocus() }
    }

    fun currentRows(): List<RowSpec> = rowsOf(section, component.model.value.settings)

    fun rowRequester(index: Int): FocusRequester? {
        val rows = currentRows()
        if (rows.isEmpty()) return null
        val spec = rows[index.coerceIn(0, rows.lastIndex)]
        // a chip row starts at its selected chip
        val chip = if (spec is ChoiceSpec) spec.selected.coerceIn(0, spec.count - 1) else 0
        return requestersFor(spec)[chip]
    }

    fun focusRow(index: Int) {
        runCatching { rowRequester(index)?.requestFocus() }
    }

    fun selectSection(index: Int) {
        section = index.coerceIn(0, Section.entries.lastIndex)
        runCatching { sectionRequesters[section].requestFocus() }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val portrait = maxHeight > maxWidth * 1.1f
        val sectionPaneWidth = maxWidth / 3

        fun onKey(event: KeyEvent): Boolean {
            val down = event.type == KeyEventType.KeyDown
            when (event.key) {
                Key.Back, Key.ButtonB, Key.Escape -> {
                    if (event.type == KeyEventType.KeyUp) {
                        if (inControls) focusSections() else component.onClose()
                    }
                    return true
                }
            }
            if (!down) return false
            // the keys that move between sections: Up/Down beside them, Left/Right on tabs
            val previousSection = if (portrait) Key.DirectionLeft else Key.DirectionUp
            val nextSection = if (portrait) Key.DirectionRight else Key.DirectionDown
            val intoControls = if (portrait) Key.DirectionDown else Key.DirectionRight
            if (!inControls) {
                when (event.key) {
                    previousSection -> selectSection(section - 1)
                    nextSection -> selectSection(section + 1)
                    intoControls -> focusControls = true
                    else -> return false
                }
                return true
            }
            val spec = currentRows().getOrNull(row) ?: return false
            when (event.key) {
                Key.DirectionUp -> if (row == 0 && portrait) focusSections() else focusRow(row - 1)
                Key.DirectionDown -> focusRow(row + 1)
                Key.DirectionLeft ->
                    when {
                        spec is SliderSpec && spec.value > 0f -> spec.onChange(step(spec.value, -1))
                        spec is ChoiceSpec && item > 0 -> runCatching { requestersFor(spec)[item - 1].requestFocus() }
                        portrait -> Unit
                        else -> focusSections()
                    }
                Key.DirectionRight ->
                    when {
                        spec is SliderSpec -> spec.onChange(step(spec.value, 1))
                        spec is ChoiceSpec && item < spec.count - 1 ->
                            runCatching { requestersFor(spec)[item + 1].requestFocus() }
                        else -> Unit
                    }
                else -> return false
            }
            return true
        }

        Surface(
            // a fresh lambda on every composition: a reference to onKey would compare equal
            // and keep the first composition's rows
            modifier = Modifier.fillMaxSize().onPreviewKeyEvent { onKey(it) },
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.fillMaxSize()) {
                TitleBar(onBack = { if (inControls) focusSections() else component.onClose() })

                val sectionList: @Composable (Modifier, Boolean) -> Unit = { modifier, horizontal ->
                    SectionList(
                        modifier = modifier,
                        horizontal = horizontal,
                        titles = sectionTitles,
                        selected = section,
                        requesters = sectionRequesters,
                        onFocused = { index ->
                            inControls = false
                            section = index
                        },
                        onClick = { index ->
                            section = index
                            focusControls = true
                        },
                    )
                }
                val controlPane: @Composable (Modifier) -> Unit = { modifier ->
                    ControlPane(
                        modifier = modifier,
                        section = section,
                        rows = rows,
                        requestersFor = ::requestersFor,
                        onFocused = { r, i ->
                            inControls = true
                            row = r
                            item = i
                        },
                    )
                }

                if (portrait) {
                    sectionList(Modifier.fillMaxWidth(), true)
                    controlPane(Modifier.fillMaxWidth().weight(1f))
                } else {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        sectionList(Modifier.width(sectionPaneWidth).fillMaxHeight(), false)
                        controlPane(Modifier.weight(1f).fillMaxHeight())
                    }
                }

                HintBar(
                    leading = "Settings",
                    hints =
                        if (inControls) {
                            listOf("D-pad" to "Move", "A" to "Change", "B" to "Back")
                        } else {
                            listOf("D-pad" to "Move", "A" to "Open", "B" to "Close")
                        },
                )
            }
        }
    }

    LaunchedEffect(section, focusControls) {
        if (focusControls) {
            rowRequester(0)?.requestFocusSoon()
            focusControls = false
        }
    }

    LaunchedEffect(Unit) {
        sectionRequesters[section].requestFocusSoon()
    }
}

/**
 * Focuses once the target is there: the panes are composed while BoxWithConstraints
 * measures, after the frame callback, and the dialog's window needs a moment too.
 */
private suspend fun FocusRequester.requestFocusSoon() {
    repeat(10) {
        withFrameNanos { }
        if (runCatching { requestFocus() }.getOrDefault(false)) return
    }
}

private fun step(
    value: Float,
    direction: Int,
): Float = (((value * SLIDER_STEPS).roundToInt() + direction).toFloat() / SLIDER_STEPS).coerceIn(0f, 1f)

@Composable
private fun TitleBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.settings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Row(
            // for touch and mouse; with a controller it's the B button
            modifier =
                Modifier
                    .clip(RowShape)
                    .focusProperties { canFocus = false }
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "B",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = stringResource(Res.string.back),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

private val RowShape = RoundedCornerShape(10.dp)
private val ChipShape = RoundedCornerShape(8.dp)

@Composable
private fun SectionList(
    modifier: Modifier,
    horizontal: Boolean,
    titles: List<String>,
    selected: Int,
    requesters: List<FocusRequester>,
    onFocused: (Int) -> Unit,
    onClick: (Int) -> Unit,
) {
    val items: @Composable (Modifier) -> Unit = { itemModifier ->
        titles.forEachIndexed { index, title ->
            val isSelected = index == selected
            Box(
                modifier =
                    itemModifier
                        .padding(4.dp)
                        .heightIn(min = 40.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            RowShape,
                        ).focusRequester(requesters[index])
                        .onFocusChanged { if (it.isFocused) onFocused(index) }
                        .focusRing(RowShape)
                        .clickable { onClick(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (horizontal) {
        Row(modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp)) { items(Modifier) }
    } else {
        Column(modifier.verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 4.dp)) {
            items(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ControlPane(
    modifier: Modifier,
    section: Int,
    rows: List<RowSpec>,
    requestersFor: (RowSpec) -> List<FocusRequester>,
    onFocused: (Int, Int) -> Unit,
) {
    // a fresh scroll position for every section
    val scroll = remember(section) { ScrollState(0) }
    Column(
        modifier =
            modifier
                .verticalScroll(scroll)
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEachIndexed { rowIndex, spec ->
            key(spec.key) {
                val focus = requestersFor(spec)
                when (spec) {
                    is SwitchSpec -> SwitchRow(spec, focus[0]) { onFocused(rowIndex, 0) }
                    is SliderSpec -> SliderRow(spec, focus[0]) { onFocused(rowIndex, 0) }
                    is ChoiceSpec -> ChoiceRow(spec, focus) { onFocused(rowIndex, it) }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    spec: SwitchSpec,
    requester: FocusRequester,
    onFocused: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .focusRequester(requester)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .focusRing(RowShape)
                .clickable { spec.onToggle(!spec.checked) }
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(spec.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        // shows the state; the whole row is what takes the press
        Switch(checked = spec.checked, onCheckedChange = null)
    }
}

@Composable
private fun SliderRow(
    spec: SliderSpec,
    requester: FocusRequester,
    onFocused: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .focusRequester(requester)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .focusRing(RowShape)
                .focusable()
                .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(spec.label, style = MaterialTheme.typography.bodyLarge)
            Text("${(spec.value * 100).roundToInt()}%", style = MaterialTheme.typography.bodyLarge)
        }
        Slider(
            value = spec.value,
            onValueChange = spec.onChange,
            valueRange = 0f..1f,
            // Left/Right on the row moves it; the slider itself is for touch and mouse
            modifier = Modifier.height(28.dp).focusProperties { canFocus = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceRow(
    spec: ChoiceSpec,
    requesters: List<FocusRequester>,
    onFocused: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
        spec.label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            spec.options.forEachIndexed { index, label ->
                TetrisFilterChip(
                    modifier =
                        Modifier
                            .focusRequester(requesters[index])
                            .onFocusChanged { if (it.isFocused) onFocused(index) }
                            .focusRing(ChipShape),
                    selected = index == spec.selected,
                    onClick = { spec.onSelect(index) },
                    label = label,
                )
            }
        }
    }
}

private fun enumLabel(value: Enum<*>): String = value.name.replace('_', ' ')

private fun rotationDirectionLabel(direction: RotationDirection): String =
    when (direction) {
        RotationDirection.CLOCKWISE -> "CW"
        RotationDirection.COUNTERCLOCKWISE -> "CCW"
        RotationDirection.ONE_EIGHTY -> "180"
    }

private fun gestureSensitivityLabel(sensitivity: GestureSensitivity): String =
    when (sensitivity) {
        GestureSensitivity.RELAXED -> "Relaxed"
        GestureSensitivity.NORMAL -> "Normal"
        GestureSensitivity.COMPETITIVE -> "Competitive"
    }

private fun musicThemeLabel(theme: MusicTheme): String =
    when (theme) {
        // the same in every language
        MusicTheme.KOROBEINIKI -> "andr36oid Коробейники"
        MusicTheme.CLASSIC -> "Classic"
        MusicTheme.MODERN -> "Modern"
        MusicTheme.MINIMAL -> "Minimal"
        MusicTheme.ARCADE -> "Arcade"
        MusicTheme.DUSK -> "Dusk"
        MusicTheme.BATTLE -> "Battle"
        MusicTheme.NONE -> "Off"
    }

@Composable
@Preview
fun SettingsPagePreview() {
    TetrisLiteAppTheme {
        SettingsPage(PreviewSettingsComponent())
    }
}
