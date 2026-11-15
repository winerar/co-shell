package app.coshell.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellChangePill
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.reports.ReportsUiState.Mark

/** «↑ 45%», «↓ 33%» or «новое» next to an amount (#238, board `Tr238MarkA`). */
@Composable
internal fun MarkPill(mark: Mark, modifier: Modifier = Modifier) {
    val text = when {
        mark.percent == null -> stringResource(R.string.report_mark_new)
        mark.up -> stringResource(R.string.report_mark_up, mark.percent)
        else -> stringResource(R.string.report_mark_down, mark.percent)
    }
    CoShellChangePill(text, markColor(mark), modifier)
}

/** More than usual — the warning colour, less — the calm one. */
@Composable
internal fun markColor(mark: Mark): Color = LocalStatusPalette.current.let { if (mark.up) it.warning else it.success }

/** What TalkBack says after the amount: «на 45 процентов больше обычного». */
@Composable
internal fun markDescription(mark: Mark): String = when {
    mark.percent == null -> stringResource(R.string.report_mark_new_description)
    mark.up -> pluralStringResource(R.plurals.report_mark_above_description, mark.percent, mark.percent)
    else -> pluralStringResource(R.plurals.report_mark_below_description, mark.percent, mark.percent)
}

/** The line next to the pill in «Из чего сложилось»: «больше обычного · к 11-му обычно 61». */
@Composable
internal fun markLine(mark: Mark): String {
    if (mark.percent == null) return stringResource(R.string.report_mark_new_line)
    val direction = stringResource(if (mark.up) R.string.report_mark_above else R.string.report_mark_below)
    val usual = mark.byDay?.let { stringResource(R.string.report_mark_usual_by, it, mark.expected) }
        ?: stringResource(R.string.report_mark_usual, mark.expected)
    return "$direction · $usual"
}
