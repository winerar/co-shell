package app.coshell.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountHeadlineStyle
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Cushion
import app.coshell.reports.ReportsUiState.CushionGloss
import app.coshell.ui.format.sentenceAmounts

/**
 * «Подушка» (#241, boards `Cush241Reports`, `Cush241Edge`): how many months the money on the accounts in the total lasts at the
 * average spending — the number, one calm phrase and a scale of half a year. Before the first full month — only the phrase.
 */
@Composable
internal fun CushionCard(cushion: Cushion, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reports_cushion_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                if (cushion.months != null) {
                    Text(
                        stringResource(R.string.reports_cushion_caption, shownAmount(cushion.money)),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        maxLines = 1,
                    )
                }
            }
            val months = cushion.months
            if (months != null) {
                val described = stringResource(R.string.reports_cushion_description, months)
                Row(
                    Modifier.padding(top = Spacing.xs).clearAndSetSemantics { contentDescription = described },
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(stringResource(R.string.reports_cushion_value, months), style = AmountHeadlineStyle, maxLines = 1)
                    Text(
                        " " + stringResource(R.string.reports_cushion_unit),
                        style = MaterialTheme.typography.titleSmall,
                        color = muted,
                        modifier = Modifier.padding(bottom = Spacing.xxs),
                    )
                }
            }
            Text(
                gloss(cushion),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            if (months != null) {
                CoShellMeter(cushion.fraction, progressColors().indicator, Modifier.padding(top = Spacing.m), grow = true)
                Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(SCALE_START, SCALE_MIDDLE, stringResource(R.string.reports_cushion_scale_end)).forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun gloss(cushion: Cushion): String = when (cushion.gloss) {
    CushionGloss.NOT_YET -> stringResource(R.string.reports_cushion_not_yet)
    CushionGloss.EMPTY -> stringResource(R.string.reports_cushion_empty)
    CushionGloss.LOW -> pluralStringResource(R.plurals.reports_cushion_low, cushion.weeks, cushion.weeks)
    CushionGloss.FEW -> pluralStringResource(R.plurals.reports_cushion_few, cushion.historyMonths, cushion.historyMonths)
    CushionGloss.NO_RATE -> stringResource(R.string.reports_cushion_no_rate, cushion.missing.joinToString())
    CushionGloss.NORMAL -> stringResource(R.string.reports_cushion_normal, cushion.monthlySpend.sentenceAmounts())
}

private const val SCALE_START = "0"
private const val SCALE_MIDDLE = "3"
