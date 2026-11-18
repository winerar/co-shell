package app.coshell.greeting

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.R as DesignR

/** The 1.0 greeting pill over the bottom bar of «Обзор» (board `HomeGreeting`); «×» closes it for good. */
@Composable
fun GreetingPill(visible: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    CoShellNoticePill(
        visible = visible,
        icon = DesignR.drawable.ic_redeem,
        text = stringResource(R.string.greeting_pill),
        onClick = onDismiss,
        dismissLabel = stringResource(R.string.greeting_dismiss),
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
