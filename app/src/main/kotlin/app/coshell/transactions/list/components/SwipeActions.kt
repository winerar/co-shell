package app.coshell.transactions.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.launch

/**
 * Swipe a row left to delete it (no confirmation, «Отменить» in the snackbar — design-questions §5) or right
 * to duplicate it (a new operation today opens; the row springs back). The strip under the row shows which.
 */
@Composable
internal fun SwipeActions(onDelete: () -> Unit, onDuplicate: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    // Tick when the swipe passes the point where letting go acts (design-questions §6).
    LaunchedEffect(state.targetValue) {
        if (state.targetValue != SwipeToDismissBoxValue.Settled) {
            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDuplicate()
                    scope.launch { state.reset() }
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        backgroundContent = {
            // Only while swiping: a pressed row sinks a little, and the strip must not peek out.
            when (state.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Box(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = Spacing.xl),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        painterResource(DesignR.drawable.ic_delete),
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                SwipeToDismissBoxValue.StartToEnd -> Row(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = Spacing.xl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    Icon(painterResource(DesignR.drawable.ic_content_copy), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(stringResource(R.string.action_duplicate), color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge)
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) { content() }
}
