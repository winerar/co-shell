package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.delay
import app.coshell.core.designsystem.R as DesignR

/** How long «Баланс исправлен» stays before leaving by itself. */
private const val SHOWN_MS = 4_000L

/**
 * «Баланс исправлен: +12 000,00 ARS» (#581, board `AccountAdjusted`): over «Сохранить» of the account sheet after
 * «Сохранить баланс» recorded the adjustment; leaves by itself or by «×». [diff] `null` — hidden.
 */
@Composable
internal fun BalanceSavedPill(diff: String?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(diff) {
        if (diff == null) return@LaunchedEffect
        delay(SHOWN_MS)
        onDismiss()
    }
    CoShellNoticePill(
        visible = diff != null,
        icon = DesignR.drawable.ic_check,
        text = diff?.let { stringResource(R.string.account_balance_saved, it) }.orEmpty(),
        onClick = onDismiss,
        dismissLabel = stringResource(R.string.action_close),
        onDismiss = onDismiss,
        modifier = modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Sizes.button + Spacing.m * 2),
    )
}
