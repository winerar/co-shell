package app.coshell.reports.review

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
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
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.RowGroupPosition
import app.coshell.core.designsystem.components.rowGroupSegment
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.review.components.LimitsTally
import app.coshell.ui.format.formatMonthOf
import app.coshell.ui.top.ScreenTopBar

/** Chips of «Лимиты октября»: «Все · 18 · Перерасход · 3 · В лимите · 15». */
enum class LimitsFilter { ALL, OVER, WITHIN }

/**
 * «Лимиты октября» (board `Mon236Limits`): every limit of the reviewed month, overspent first, with chips and the
 * tally on top. Each limit is one lazy item drawing its piece of the card, so 30 limits scroll lightly.
 */
@Composable
fun MonthLimitsScreen(state: MonthReviewUiState, onBack: () -> Unit, onLimit: (categoryId: String) -> Unit) {
    val month = state.month
    var filter by rememberSaveable { mutableStateOf(LimitsFilter.ALL) }
    Scaffold(
        topBar = { ScreenTopBar(month?.let { stringResource(R.string.review_limits_title, formatMonthOf(it)) }.orEmpty(), onBack = onBack) },
    ) { padding ->
        val limits = state.limits ?: return@Scaffold
        val shown = limits.all.filter { it.matches(filter) }
        val side = Modifier.padding(horizontal = Spacing.l)
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            item(key = "filter") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    LimitsFilter.entries.forEach { option ->
                        val count = limits.all.count { it.matches(option) }
                        if (count == 0 && option != LimitsFilter.ALL) return@forEach
                        CoShellFilterChip(filter == option, { filter = option }, { Text(stringResource(filterLabel(option), count)) })
                    }
                }
            }
            item(key = "tally") { CoShellCard(side.fillMaxWidth().padding(vertical = Spacing.m)) { LimitsTally(limits) } }
            itemsIndexed(shown, key = { _, envelope -> envelope.categoryId }) { index, envelope ->
                Column(side.fillMaxWidth().rowGroupSegment(RowGroupPosition(index, shown.size))) {
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    EnvelopeRow(envelope, onClick = { onLimit(envelope.categoryId) })
                }
            }
        }
    }
}

private fun Envelope.matches(filter: LimitsFilter): Boolean = when (filter) {
    LimitsFilter.ALL -> true
    LimitsFilter.OVER -> tone == BulletTone.DANGER
    LimitsFilter.WITHIN -> tone != BulletTone.DANGER
}

private fun filterLabel(filter: LimitsFilter): Int = when (filter) {
    LimitsFilter.ALL -> R.string.budgets_filter_all
    LimitsFilter.OVER -> R.string.review_filter_over
    LimitsFilter.WITHIN -> R.string.review_filter_within
}
