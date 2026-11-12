package app.coshell.budgets.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.list.BudgetFilter
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.tokens.Spacing

/** «Все · 18 · Горит · 3 · В плане · 13 · Без трат · 2» (`BudMany`); a chip with nothing behind it is not shown. */
@Composable
internal fun BudgetFilterChips(
    counts: Map<BudgetFilter, Int>,
    selected: BudgetFilter,
    onSelect: (BudgetFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        BudgetFilter.entries.forEach { filter ->
            val count = counts[filter] ?: 0
            if (count == 0 && filter != BudgetFilter.ALL) return@forEach
            CoShellFilterChip(selected == filter, { onSelect(filter) }, { Text(stringResource(label(filter), count)) })
        }
    }
}

private fun label(filter: BudgetFilter): Int = when (filter) {
    BudgetFilter.ALL -> R.string.budgets_filter_all
    BudgetFilter.HOT -> R.string.budgets_filter_hot
    BudgetFilter.IN_PLAN -> R.string.budgets_filter_in_plan
    BudgetFilter.IDLE -> R.string.budgets_filter_idle
}
