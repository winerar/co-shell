package app.coshell.reports.edit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellReorderRow
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.ReorderState
import app.coshell.core.domain.settings.ReportCard
import app.coshell.core.domain.settings.ReportLayout
import app.coshell.ui.order.DragHandle
import app.coshell.core.designsystem.R as DesignR

// The list of «Отчёты» cards (#826, board Rep826Compact): the shown ones with ⠿ and ✕ (hold to drag), then «Скрыты» with «+».
// Both the editing mode of the feed (`ReportsEditList`) and «Настройки → Экраны → Отчёты» draw it inside a CoShellReorderColumn.

/**
 * The rows of [layout] for a [CoShellReorderColumn] whose keys are [ReportLayout.visible]; [onChange] gets every new layout;
 * [rowModifier] — on every row (the side padding of a screen whose column has none).
 */
@Composable
internal fun ColumnScope.ReportCardRows(layout: ReportLayout, state: ReorderState, onChange: (ReportLayout) -> Unit, rowModifier: Modifier = Modifier) {
    val shown = layout.visible
    shown.forEachIndexed { i, card ->
        CoShellReorderRow(
            state = state,
            key = card,
            modifier = rowModifier,
            moveUpLabel = stringResource(R.string.order_move_up),
            onMoveUp = if (i > 0) ({ onChange(layout.moveShown(card, i - 1)) }) else null,
            moveDownLabel = stringResource(R.string.order_move_down),
            onMoveDown = if (i < shown.lastIndex) ({ onChange(layout.moveShown(card, i + 1)) }) else null,
        ) {
            CoShellListRow(
                title = stringResource(card.titleRes()),
                subtitle = stringResource(card.hintRes()),
                leading = { DragHandle() },
                dense = true,
                trailing = {
                    CoShellIconButton(onClick = { onChange(layout.withShown(card, false)) }) {
                        Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.home_edit_hide))
                    }
                },
            )
        }
    }
    val hidden = layout.hiddenCards
    if (hidden.isNotEmpty()) {
        CoShellSectionLabel(stringResource(R.string.report_cards_hidden), rowModifier.padding(top = Spacing.s))
        hidden.forEach { card ->
            CoShellListRow(
                title = stringResource(card.titleRes()),
                subtitle = stringResource(card.hintRes()),
                modifier = rowModifier,
                dense = true,
                trailing = {
                    CoShellIconButton(onClick = { onChange(layout.withShown(card, true)) }) {
                        Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.home_edit_show))
                    }
                },
            )
        }
    }
}

/** The name of a card in the lists (without the period or currencies some card titles carry). */
internal fun ReportCard.titleRes(): Int = when (this) {
    ReportCard.SPENDING -> R.string.report_card_spending
    ReportCard.UNUSUAL -> R.string.report_card_unusual
    ReportCard.CONVERSION -> R.string.report_card_conversion
    ReportCard.MONTHS -> R.string.report_card_months
    ReportCard.INFLATION -> R.string.report_card_inflation
    ReportCard.INFLATION_GROWTH -> R.string.report_card_growth
    ReportCard.INCOME -> R.string.report_card_income
    ReportCard.CAPITAL -> R.string.report_card_capital
    ReportCard.CUSHION -> R.string.report_card_cushion
    ReportCard.FORECAST -> R.string.report_card_forecast
    ReportCard.FIXED -> R.string.report_card_fixed
    ReportCard.MEMBERS -> R.string.report_card_members
}

/** One line under the name: what the card shows. */
internal fun ReportCard.hintRes(): Int = when (this) {
    ReportCard.SPENDING -> R.string.report_card_spending_hint
    ReportCard.UNUSUAL -> R.string.report_card_unusual_hint
    ReportCard.CONVERSION -> R.string.report_card_conversion_hint
    ReportCard.MONTHS -> R.string.report_card_months_hint
    ReportCard.INFLATION -> R.string.report_card_inflation_hint
    ReportCard.INFLATION_GROWTH -> R.string.report_card_growth_hint
    ReportCard.INCOME -> R.string.report_card_income_hint
    ReportCard.CAPITAL -> R.string.report_card_capital_hint
    ReportCard.CUSHION -> R.string.report_card_cushion_hint
    ReportCard.FORECAST -> R.string.report_card_forecast_hint
    ReportCard.FIXED -> R.string.report_card_fixed_hint
    ReportCard.MEMBERS -> R.string.report_card_members_hint
}
