package app.coshell.ui.top

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTopBar
import app.coshell.core.designsystem.components.TopBarKind
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.R as DesignR

/**
 * Header of a list in selection mode (mockup 19В): «✕» ends the mode, the title counts the chosen rows.
 * Takes the place of [TabTopBar] while something is selected — the screen slides it down over the tab
 * header ([TabOrSelectionTopBar]); the number rolls up when it grows and down when it falls (header poll А).
 * Solid raised fill, so the mode is visible at a glance.
 */
@Composable
fun SelectionTopBar(count: Int, onClear: () -> Unit) {
    CoShellTopBar(
        kind = TopBarKind.Selection,
        navigation = {
            CoShellIconButton(onClick = onClear) {
                Icon(painterResource(DesignR.drawable.ic_close), contentDescription = stringResource(R.string.transactions_selection_clear))
            }
        },
    ) { style -> SelectedCount(count, style) }
}

/** «Выбрано 3» with only the number rolling; the words around it come from the translated string. */
@Composable
private fun SelectedCount(count: Int, style: TextStyle) {
    val full = stringResource(R.string.transactions_selected, count)
    val digits = count.toString()
    val at = full.indexOf(digits)
    if (at < 0) {
        Text(full, style = style)
        return
    }
    // Read aloud and found by tests as one phrase, not three pieces.
    Row(Modifier.clearAndSetSemantics { text = AnnotatedString(full) }, verticalAlignment = Alignment.CenterVertically) {
        if (at > 0) Text(full.substring(0, at), style = style)
        RollingNumber(count, style)
        if (at + digits.length < full.length) Text(full.substring(at + digits.length), style = style)
    }
}

@Composable
private fun RollingNumber(value: Int, style: TextStyle) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically(Motion.Placement) { if (up) it else -it } + fadeIn(Motion.effects()))
                .togetherWith(slideOutVertically(Motion.Placement) { if (up) -it else it } + fadeOut(Motion.effectsFast()))
                .using(SizeTransform(clip = true))
        },
        label = "selectedCount",
    ) { Text(it.toString(), style = style) }
}
