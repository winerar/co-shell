package app.coshell.reports.all

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState
import app.coshell.reports.categoryName
import app.coshell.reports.periodTitle
import app.coshell.ui.top.ScreenTopBar

/**
 * «Все категории» (board `RepAll`): every category of the report's period, flat, as bars on one scale with the
 * previous period's mark; search and «По сумме · По росту · Группами» for a long list (24 categories and more).
 */
@Composable
fun ReportCategoriesScreen(state: ReportsUiState, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var order by rememberSaveable { mutableStateOf(RowOrder.AMOUNT) }
    val rows = state.spending?.categories.orEmpty()
    val deleted = stringResource(R.string.reports_deleted_category)
    val shown = orderRows(rows, query, order, nameOf = { it.name ?: deleted })
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.reports_all_title), onBack = onBack, subtitle = state.period?.let { periodTitle(it) }) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item(key = "search") {
                CoShellSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.reports_all_search, rows.size),
                    clearLabel = stringResource(R.string.search_clear),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "order") { OrderChips(order, onOrder = { order = it }) }
            item(key = "rows") {
                CoShellCard(Modifier.fillMaxWidth()) {
                    if (shown.isEmpty()) {
                        Text(
                            stringResource(R.string.reports_all_nothing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Spacing.l),
                        )
                    }
                    shown.forEach { CategoryBar(it, Modifier.padding(start = if (it.inner) Spacing.xxl else Spacing.l, end = Spacing.l)) }
                }
            }
        }
    }
}

@Composable
private fun OrderChips(order: RowOrder, onOrder: (RowOrder) -> Unit) {
    val labels = mapOf(
        RowOrder.AMOUNT to R.string.reports_order_amount,
        RowOrder.GROWTH to R.string.reports_order_growth,
        RowOrder.GROUPS to R.string.reports_order_groups,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        labels.forEach { (value, label) ->
            CoShellFilterChip(selected = order == value, onClick = { onOrder(value) }, label = { Text(stringResource(label)) })
        }
    }
}

@Composable
private fun CategoryBar(item: OrderedRow, modifier: Modifier = Modifier) {
    val row: ReportsUiState.CategoryRow = item.row
    CoShellBarRow(
        label = categoryName(row.name),
        value = row.value,
        share = "${row.percent}%",
        fraction = row.fraction,
        color = row.color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.primary,
        ghost = row.ghost,
        ghostDescription = row.ghostValue?.let { stringResource(R.string.reports_ghost_value, it) },
        leading = { CategoryBadge(row.icon, row.color, size = Sizes.badgeSmall) },
        modifier = modifier,
    )
}
