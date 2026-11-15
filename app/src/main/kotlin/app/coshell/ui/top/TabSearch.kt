package app.coshell.ui.top

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.R as DesignR

/**
 * Search that lives in a tab header (#394 Б): a loupe among the actions; tapped, the field opens in place of the
 * title, the list filters as you type, «✕» (or back) clears and closes it. All texts come from the screen.
 */
@Immutable
data class TabSearch(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val placeholder: String,
    val openLabel: String,
    val clearLabel: String,
    val closeLabel: String,
)

/** The loupe that opens the field. */
@Composable
internal fun SearchOpenButton(search: TabSearch, onOpen: () -> Unit) {
    CoShellIconButton(onClick = onOpen) { Icon(painterResource(DesignR.drawable.ic_search), search.openLabel) }
}

/** «✕»: empties the query and folds the field back into the loupe. */
@Composable
internal fun SearchCloseButton(search: TabSearch, onClose: () -> Unit) {
    CoShellIconButton(onClick = { search.onQueryChange(""); onClose() }) { Icon(painterResource(DesignR.drawable.ic_close), search.closeLabel) }
}

/** The field in the header, focused as it opens so the keyboard comes up at once. */
@Composable
internal fun HeaderSearchField(search: TabSearch, onClose: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler { search.onQueryChange(""); onClose() }
    CoShellSearchField(
        value = search.query,
        onValueChange = search.onQueryChange,
        placeholder = search.placeholder,
        clearLabel = search.clearLabel,
        modifier = Modifier.focusRequester(focus),
    )
}
