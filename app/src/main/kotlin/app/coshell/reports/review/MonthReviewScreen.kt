package app.coshell.reports.review

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.review.components.ChangesCard
import app.coshell.reports.review.components.LimitsCard
import app.coshell.reports.review.components.ReviewHero
import app.coshell.ui.format.formatMonthOf
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/** Everything «Итог месяца» opens: back, «Из чего сложилось» of a change, an envelope, «Все лимиты». */
data class MonthReviewActions(
    val onBack: () -> Unit = {},
    val onChange: (categoryId: String) -> Unit = {},
    val onLimit: (categoryId: String) -> Unit = {},
    val onAllLimits: () -> Unit = {},
)

/**
 * «Итог октября» (boards `Mon236Screen`, `Mon236Edge`): what was left, the biggest changes against the usual months,
 * the limits; without history — a line instead of the changes, without limits — no block. A note on the rate at the end.
 */
@Composable
fun MonthReviewScreen(state: MonthReviewUiState, actions: MonthReviewActions = MonthReviewActions()) {
    val month = state.month
    Scaffold(
        topBar = { ScreenTopBar(month?.let { stringResource(R.string.review_title, formatMonthOf(it)) }.orEmpty(), onBack = actions.onBack) },
    ) { padding ->
        if (state.isLoading || month == null) return@Scaffold
        val side = Modifier.padding(horizontal = Spacing.l)
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(top = Spacing.xs, bottom = Spacing.xl)) {
            item(key = "hero") { ReviewHero(state, side) }
            if (state.usualMonths == 0) {
                item(key = "noHistory") {
                    CoShellMessage(stringResource(R.string.review_no_history), isError = false, modifier = side.padding(top = Spacing.l))
                }
            } else {
                item(key = "changesTitle") { SectionTitle(stringResource(R.string.review_changes, state.usualMonths)) }
                item(key = "changes") {
                    if (state.changes.isEmpty()) CoShellMessage(stringResource(R.string.review_no_changes), isError = false, modifier = side)
                    else ChangesCard(state.changes, actions.onChange, side)
                }
            }
            state.limits?.let { limits ->
                item(key = "limitsTitle") { SectionTitle(stringResource(R.string.review_limits)) }
                item(key = "limits") {
                    LimitsCard(limits, stringResource(R.string.review_all_limits, formatMonthOf(month)), actions.onAllLimits, actions.onLimit, side)
                }
            }
            item(key = "note") {
                val note = stringResource(R.string.reports_rate_note, state.currency)
                val text = if (state.missing.isEmpty()) note else note + ". " + stringResource(R.string.reports_missing, state.missing.joinToString())
                CoShellHint(text, side.padding(vertical = Spacing.m))
            }
        }
    }
}
