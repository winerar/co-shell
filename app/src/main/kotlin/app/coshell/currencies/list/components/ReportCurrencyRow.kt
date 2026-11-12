package app.coshell.currencies.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import java.util.Currency
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * One report currency as a card (mockup 12): the code in the numbers font, the main one outlined in the
 * palette's primary with an «Основная» pill; a tap on another makes it main, the bin removes it.
 */
@Composable
internal fun ReportCurrencyRow(
    code: String,
    isMain: Boolean,
    canRemove: Boolean,
    onMakeMain: () -> Unit,
    onRemove: () -> Unit,
) {
    CoShellCard(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xs),
        tint = if (isMain) MaterialTheme.colorScheme.primary else null,
        onClick = if (isMain) null else onMakeMain,
    ) {
        Row(
            Modifier.heightIn(min = Sizes.rowMinHeight).padding(start = Spacing.l, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text(code, style = AmountTitleStyle)
            Text(
                currencyName(code).orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (isMain) {
                Text(
                    stringResource(R.string.report_currencies_main),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, PillShape)
                        .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                )
            }
            if (canRemove) {
                CoShellIconButton(onClick = onRemove) {
                    Icon(painterResource(DesignR.drawable.ic_delete), stringResource(R.string.action_delete))
                }
            }
        }
    }
}

/** «доллар США» in the phone's language; `null` for codes Java doesn't know (USDT and other crypto). */
private fun currencyName(code: String): String? =
    runCatching { Currency.getInstance(code).displayName }.getOrNull()?.takeIf { it != code }
