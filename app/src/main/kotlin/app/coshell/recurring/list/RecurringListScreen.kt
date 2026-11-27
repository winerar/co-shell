package app.coshell.recurring.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.WordHint
import app.coshell.hints.LocalHintHost
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellAddFab
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.recurring.fixed.FixedRow
import app.coshell.recurring.fixed.FixedTotal
import app.coshell.recurring.fixed.FixedUi
import app.coshell.recurring.fixed.fixedCount
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.recurring.list.components.MonthAheadCard
import app.coshell.recurring.list.components.PausedMessage
import app.coshell.recurring.list.components.RecurringPaymentRow
import app.coshell.ui.format.formatMonth
import app.coshell.ui.format.formatMonthYear
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import kotlinx.datetime.YearMonth

/**
 * «Регулярные платежи» (#143, board `Rec143List`): search (from [SEARCH_FROM] rules), the hero «До конца ноября»,
 * chips «Все · Расходы · Доходы · Само», then rules by the month of their next payment; a payment on pause (account
 * archived, #406) — a message above the hero. A tap opens the template,
 * «+» creates one (the «Повтор» is set in it).
 */
@Composable
fun RecurringListScreen(state: RecurringListUiState, actions: RecurringListActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.recurring_list_title), onBack = actions.onBack)
        },
        floatingActionButton = { CoShellAddFab(stringResource(R.string.recurring_list_new), actions.onNew) },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        if (state.isEmpty) {
            Column(Modifier.padding(underBars(padding))) {
                WordHint(HintId.RECURRING, R.string.hint_recurring, Modifier.padding(horizontal = Spacing.l), emptyScreen = true)
                EmptyList()
            }
            return@Scaffold
        }
        val hintsOn = LocalHintHost.current != null
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(bottom = Sizes.fabClearance)),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (hintsOn) item(key = "word-hint") { WordHint(HintId.RECURRING, R.string.hint_recurring, Modifier.padding(horizontal = Spacing.l)) }
            item(key = "mode") {
                CoShellSegmented(
                    listOf(stringResource(R.string.recurring_mode_month), stringResource(R.string.recurring_mode_year)),
                    state.mode.ordinal,
                    { actions.onModeChange(RecurringMode.entries[it]) },
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                )
            }
            if (state.mode == RecurringMode.YEAR) {
                yearItems(state.fixed, actions.onEdit)
                return@LazyColumn
            }
            if (state.total >= SEARCH_FROM) {
                item(key = "search") {
                    CoShellSearchField(
                        value = state.query,
                        onValueChange = actions.onQueryChange,
                        placeholder = stringResource(R.string.recurring_list_search),
                        clearLabel = stringResource(R.string.search_clear),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                    )
                }
            }
            state.paused.forEach { paused ->
                item(key = "paused-${paused.templateId}") {
                    PausedMessage(paused, { actions.onEdit(paused.templateId) }, Modifier.padding(horizontal = Spacing.l))
                }
            }
            state.ahead?.let { ahead -> item(key = "ahead") { MonthAheadCard(ahead, Modifier.padding(horizontal = Spacing.l)) } }
            if (state.counts.size > 2) {
                item(key = "filter") { FilterChips(state, actions.onFilterChange) }
            }
            state.months.forEach { group ->
                item(key = "month-${group.month}") {
                    Column {
                        SectionTitle(monthTitle(group.month, state.ahead?.month?.year))
                        CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
                            group.rows.forEachIndexed { index, row ->
                                key(row.item.id) {
                                    if (index > 0) CoShellDivider()
                                    RecurringPaymentRow(row, onClick = { actions.onEdit(row.item.id) })
                                }
                            }
                        }
                    }
                }
            }
            if (state.nothingFound) {
                item(key = "nothing") {
                    Text(
                        stringResource(R.string.picker_nothing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.l),
                    )
                }
            }
        }
    }
}

/** «За год» (#771, board `Fixed771Year`): the hero with the year, the month and the share, then every payment, the dearest first. */
private fun LazyListScope.yearItems(fixed: FixedUi?, onEdit: (String) -> Unit) {
    if (fixed == null) {
        item(key = "year-empty") {
            Text(
                stringResource(R.string.fixed_year_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
        return
    }
    item(key = "year-hero") {
        CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), tone = CardTone.Hero) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(stringResource(R.string.fixed_year_title, fixedCount(fixed.count)), style = MaterialTheme.typography.labelLarge)
                FixedTotal(fixed, amountColor = LocalHeroPalette.current.amount)
            }
        }
    }
    item(key = "year-rows") {
        CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
            Column {
                fixed.rows.forEachIndexed { index, row ->
                    key(row.item.id) {
                        if (index > 0) CoShellDivider()
                        FixedRow(row) { onEdit(row.item.id) }
                    }
                }
            }
        }
    }
}

/** «Все · 24», «Расходы · 20», «Доходы · 4», «Переводы · 1», «Само · 7» — only chips with rules. */
@Composable
private fun FilterChips(state: RecurringListUiState, onSelect: (PaymentFilter) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        PaymentFilter.entries.forEach { chip ->
            val count = state.counts[chip] ?: return@forEach
            CoShellFilterChip(state.filter == chip, { onSelect(chip) }, { Text(stringResource(chipLabel(chip), count)) })
        }
    }
}

private fun chipLabel(chip: PaymentFilter): Int = when (chip) {
    PaymentFilter.ALL -> R.string.templates_filter_all
    PaymentFilter.EXPENSE -> R.string.templates_filter_expense
    PaymentFilter.INCOME -> R.string.templates_filter_income
    PaymentFilter.TRANSFER -> R.string.templates_filter_transfer
    PaymentFilter.AUTO -> R.string.recurring_filter_auto
}

/** «Ноябрь»; another year — «Март 2027». */
private fun monthTitle(month: YearMonth, currentYear: Int?): String =
    if (month.year == currentYear) formatMonth(month.firstDay).replaceFirstChar { it.titlecase() } else formatMonthYear(month)

@Composable
private fun EmptyList(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Text(
            stringResource(R.string.recurring_list_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
