package app.coshell.reports.components

import androidx.compose.runtime.key
import app.coshell.ui.motion.SwapText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Mark
import app.coshell.reports.UnusualLine
import app.coshell.reports.categoryName

/**
 * «Необычное за ноябрь» (#787, poll 2026-11-27 «А», board `Smart787Card`): up to three folders that spend far more or less than
 * usual, under «На что ушли деньги». A bar opens the category, as there. No card when nothing stands out.
 */
@Composable
internal fun UnusualCard(lines: List<UnusualLine>, month: String, onCategory: (String) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            SwapText(
                stringResource(R.string.reports_unusual_title, month),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() },
            )
            UnusualLines(lines, onCategory)
            Text(
                stringResource(R.string.reports_unusual_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The rows themselves — the card above and the story «Не как обычно» (#787) draw the same. */
@Composable
internal fun UnusualLines(lines: List<UnusualLine>, onCategory: ((String) -> Unit)? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        lines.forEach { line -> key(line.categoryId) {
            CoShellBarRow(
                label = categoryName(line.name),
                value = line.value,
                fraction = line.fraction,
                color = line.color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.primary,
                ghost = line.usual,
                ghostDescription = usualPart(line.mark),
                leading = { CategoryBadge(line.icon, line.color, size = Sizes.badgeSmall) },
                caption = unusualCaption(line.mark),
                onClick = onCategory?.let { { it(line.categoryId) } },
            )
        } }
    }
}

/**
 * «примерно в 2 раза больше обычного», «больше обычного на 60 %», «меньше обычного на 30 % — хорошо идёте», «раньше трат не было».
 * The usual amount itself is the grey mark on the bar (and TalkBack says it): one line under the bar stays whole.
 */
@Composable
internal fun unusualCaption(mark: Mark): String {
    val percent = mark.percent ?: return stringResource(R.string.report_mark_new_line)
    return when {
        !mark.up -> stringResource(R.string.reports_unusual_less, percent)
        percent >= TIMES_FROM -> {
            val times = (PERCENT + percent + PERCENT / 2) / PERCENT
            pluralStringResource(R.plurals.reports_unusual_times, times, times)
        }
        else -> stringResource(R.string.reports_unusual_more, percent)
    }
}

@Composable
private fun usualPart(mark: Mark): String =
    mark.byDay?.let { stringResource(R.string.report_mark_usual_by, it, shownAmount(mark.expected)) }
        ?: stringResource(R.string.report_mark_usual, shownAmount(mark.expected))

private const val PERCENT = 100

/** From «+90 %» the difference reads better as «примерно в 2 раза». */
private const val TIMES_FROM = 90
