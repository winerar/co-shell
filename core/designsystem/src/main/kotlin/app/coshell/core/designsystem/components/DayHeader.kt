package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.amount.shownAmount
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
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Day title in accent capitals with the day's net on the right (board `CompData`, «Строка-столбик» family, #367 А).
 * The title keeps its width; a long net of several currencies wraps after a «·» (aligned to the end), never the title.
 * [background] — the list's surface, so the sticky header hides what scrolls under it; inside a card — transparent.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoShellDayHeader(
    title: String,
    total: String?,
    totalColor: Color,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
) {
    // A flow, not a row: the net stays beside the title while its widest amount fits there, otherwise it moves
    // under the title at full width. Either way it takes the rest of its line and wraps only after a «·».
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        CoShellSectionLabel(title, Modifier.alignByBaseline(), tone = SectionTone.Accent)
        Text(
            shownAmount(total.orEmpty()),
            style = RateTextStyle,
            color = totalColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).alignByBaseline(),
        )
    }
}
