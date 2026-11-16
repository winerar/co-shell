package app.coshell.transactions.list.components

import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.toJavaLocalDate

/**
 * Day title in accent capitals: "СЕГОДНЯ", "ВЧЕРА" or a short date, with the day's net on the right.
 * The title keeps its width; a long net of several currencies wraps after a «·» (aligned to the end), never the title.
 * [background] — the list's surface; inside a card that draws its own fill — transparent.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayHeader(day: Day, background: Color = MaterialTheme.colorScheme.surface) {
    val text = dayLabel(day)
    // A flow, not a row: the net stays beside the title while its widest amount fits there, otherwise it moves
    // under the title at full width. Either way it takes the rest of its line and wraps only after a «·».
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        CoShellSectionLabel(text, Modifier.alignByBaseline(), tone = SectionTone.Accent)
        Text(
            day.total.orEmpty(),
            style = RateTextStyle,
            color = when (day.totalTone) {
                Kind.EXPENSE -> LocalAmountPalette.current.expense
                Kind.INCOME -> LocalAmountPalette.current.income
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).alignByBaseline(),
        )
    }
}

/** «Сегодня», «Вчера» or «10 ноября, пн» — the day's name in [DayHeader] and in the tab header's second line. */
@Composable
internal fun dayLabel(day: Day): String = when (day.relative) {
    RelativeDay.TODAY -> stringResource(R.string.transaction_date_today)
    RelativeDay.YESTERDAY -> stringResource(R.string.transaction_date_yesterday)
    RelativeDay.OTHER -> dayTitle(day.date.toJavaLocalDate(), LocalDate.now().year, LocalConfiguration.current.locales[0])
}

/** "10 ноября, пн"; the year is added only when it is not [currentYear]. */
internal fun dayTitle(date: LocalDate, currentYear: Int, locale: Locale): String {
    val pattern = if (date.year == currentYear) "d MMMM, EE" else "d MMMM yyyy, EE"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}
