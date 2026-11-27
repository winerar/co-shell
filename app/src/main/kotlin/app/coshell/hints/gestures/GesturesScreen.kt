package app.coshell.hints.gestures

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/** One line of the cheat sheet: what to do ([title]) and what happens ([text]). */
internal data class Gesture(@StringRes val title: Int, @StringRes val text: Int)

/** A screen (or «Везде») and its gestures. */
internal data class GestureGroup(@StringRes val title: Int, val gestures: List<Gesture>)

/** Every gesture of the app by screen; the words of each match its hint (`hints.md`). */
internal val GestureGroups = listOf(
    GestureGroup(R.string.gestures_home, listOf(Gesture(R.string.gesture_flip_title, R.string.gesture_flip_text), Gesture(R.string.gesture_edit_home_title, R.string.gesture_edit_home_text))),
    GestureGroup(R.string.gestures_plus, listOf(Gesture(R.string.gesture_plus_tap_title, R.string.gesture_plus_tap_text), Gesture(R.string.gesture_plus_hold_title, R.string.gesture_plus_hold_text))),
    GestureGroup(
        R.string.gestures_operations,
        listOf(
            Gesture(R.string.gesture_edit_title, R.string.gesture_edit_text),
            Gesture(R.string.gesture_select_title, R.string.gesture_select_text),
            Gesture(R.string.gesture_amount_title, R.string.gesture_amount_text),
        ),
    ),
    GestureGroup(
        R.string.gestures_everywhere,
        listOf(
            Gesture(R.string.gesture_swipe_title, R.string.gesture_swipe_text),
            Gesture(R.string.gesture_shake_title, R.string.gesture_shake_text),
            Gesture(R.string.gesture_shortcuts_title, R.string.gesture_shortcuts_text),
        ),
    ),
)

/**
 * «Ещё → Настройки → Помощь → Жесты» (#797, board `Gest797`, poll 2026-11-27 «А»): the cheat sheet of every gesture by
 * screen, for re-reading what the hints said once.
 */
@Composable
fun GesturesScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.gestures_title), onBack = onBack) }) { padding ->
        LazyColumn(contentPadding = underBars(padding, PaddingValues(bottom = Spacing.xl))) {
            GestureGroups.forEach { group ->
                item(key = "title-${group.title}") { SectionTitle(stringResource(group.title)) }
                item(key = "group-${group.title}") {
                    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
                        group.gestures.forEach { gesture ->
                            row { CoShellListRow(title = { Text(stringResource(gesture.title)) }, supporting = { Text(stringResource(gesture.text)) }) }
                        }
                    }
                }
            }
        }
    }
}
