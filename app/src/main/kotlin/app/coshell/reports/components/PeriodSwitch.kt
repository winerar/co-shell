package app.coshell.reports.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «‹ Ноябрь 2026 › · USD ▾» (board `RepA`): arrows step by the period's own kind; the title and the currency pill
 * open «Период и валюта».
 */
@Composable
internal fun PeriodSwitch(
    title: String,
    currency: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        // The currency pill goes under «‹ Ноябрь 2026 ›» on a narrow screen or with a big font: the month keeps its words.
        val stacked = maxWidth < Sizes.narrowRow * LocalDensity.current.fontScale.coerceAtLeast(1f)
        val pill: @Composable (Modifier) -> Unit = { m ->
            CoShellFilterChip(selected = false, onClick = onOpenSheet, label = { Text("$currency ▾") }, modifier = m)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CoShellIconButton(onClick = onPrevious) {
                    Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.reports_previous), Modifier.rotate(180f))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .tactileClickable(Tactile.Control, role = Role.Button, onClick = onOpenSheet)
                        .padding(vertical = Spacing.s),
                )
                CoShellIconButton(onClick = onNext) {
                    Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.reports_next))
                }
                if (!stacked) pill(Modifier.padding(start = Spacing.xs))
            }
            if (stacked) pill(Modifier)
        }
    }
}
