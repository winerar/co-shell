package app.coshell.ui.order

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellReorderColumn
import app.coshell.core.designsystem.components.ReorderDrop
import app.coshell.core.designsystem.components.ReorderState
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/**
 * «Порядок счетов» / «Порядок категорий» (#338, boards `Fb338Acc`, `Fb338Cat`): the list in its drag mode — a title with
 * «✓ Готово», one line of help, then the rows. Every drop is saved at once; «Готово» and «Назад» only leave the mode.
 */
@Composable
internal fun OrderScreen(
    title: String,
    hint: String,
    state: ReorderState,
    keys: List<Any>,
    onDrop: (ReorderDrop) -> Unit,
    onDone: () -> Unit,
    canNest: (Any, Any) -> Boolean = { _, _ -> false },
    rows: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(onBack = onDone)
    Scaffold(
        topBar = {
            ScreenTopBar(title, onBack = onDone) {
                CoShellIconButton(onClick = onDone) {
                    Icon(painterResource(DesignR.drawable.ic_check), stringResource(R.string.order_done))
                }
            }
        },
    ) { padding ->
        CoShellReorderColumn(
            state = state,
            keys = keys,
            onDrop = onDrop,
            canNest = canNest,
            modifier = Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Spacing.l)),
        ) {
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            rows()
        }
    }
}

/** The ⠿ at the end of a row in «Порядок»: the row can be dragged. */
@Composable
internal fun DragHandle() {
    Icon(painterResource(DesignR.drawable.ic_drag_indicator), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
}
