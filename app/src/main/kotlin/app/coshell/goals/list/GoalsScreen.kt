package app.coshell.goals.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellAddFab
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellEmptyState
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.hints.HintId
import app.coshell.goals.GoalRow
import app.coshell.goals.GoalRowUi
import app.coshell.goals.GoalTotalsUi
import app.coshell.hints.WordHint
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** What the screen can ask for; defaults keep previews short. */
data class GoalsActions(
    val onBack: () -> Unit = {},
    val onAdd: () -> Unit = {},
    val onOpen: (String) -> Unit = {},
    val onToggleReached: () -> Unit = {},
)

/**
 * «Цели» (#768, boards `GoalListA`, `GoalMany`, `GoalEmpty`): the totals on the theme's hero card, the goals by deadline,
 * reached goals folded at the bottom. The «+» opens the goal sheet.
 */
@Composable
fun GoalsScreen(state: GoalsUiState, actions: GoalsActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.goals_title), onBack = actions.onBack) },
        floatingActionButton = { CoShellAddFab(stringResource(R.string.goals_add), actions.onAdd) },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        if (state.isEmpty) {
            Column(Modifier.padding(underBars(padding))) {
                WordHint(HintId.GOALS, R.string.hint_goals, Modifier.padding(horizontal = Spacing.l), emptyScreen = true)
                CoShellEmptyState(iconRes = DesignR.drawable.ic_flag, text = stringResource(R.string.goals_empty))
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(top = Spacing.s, bottom = Spacing.l)),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item(key = "word-hint", contentType = "hint") { WordHint(HintId.GOALS, R.string.hint_goals, Modifier.padding(horizontal = Spacing.l)) }
            item(key = "totals", contentType = "hero") { GoalTotalsCard(state.totals) }
            if (state.active.isNotEmpty()) item(key = "active", contentType = "card") { GoalsCard(state.active, actions.onOpen) }
            if (state.reached.isNotEmpty()) {
                item(key = "reachedToggle", contentType = "link") {
                    Column(Modifier.padding(horizontal = Spacing.xl)) {
                        CoShellSectionLink(
                            if (state.reachedShown) stringResource(R.string.goals_reached_hide) else stringResource(R.string.goals_reached_show, state.reached.size),
                            actions.onToggleReached,
                        )
                    }
                }
                if (state.reachedShown) item(key = "reached", contentType = "card") { GoalsCard(state.reached, actions.onOpen) }
            }
        }
    }
}

/** «Накоплено» and «Откладывать в месяц» in the main currency, by today's rates (board `GoalListA`). */
@Composable
private fun GoalTotalsCard(totals: GoalTotalsUi) {
    val hero = LocalHeroPalette.current
    CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), tone = CardTone.Hero) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Cell(stringResource(R.string.goals_saved), totals.saved, Modifier.weight(1f))
                if (totals.perMonth.isNotEmpty()) Cell(stringResource(R.string.goals_per_month), stringResource(R.string.goals_approx, totals.perMonth), Modifier.weight(1f))
            }
            val note = if (totals.missing.isEmpty()) stringResource(R.string.goals_by_today_rate) else stringResource(R.string.goals_without, totals.missing)
            Text(note, style = MaterialTheme.typography.bodySmall, color = hero.hint)
        }
    }
}

@Composable
private fun Cell(label: String, amount: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalHeroPalette.current.hint)
        AmountText(amount, AmountTitleStyle, color = LocalHeroPalette.current.amount)
    }
}

@Composable
internal fun GoalsCard(rows: List<GoalRowUi>, onOpen: (String) -> Unit, meter: Boolean = rows.size <= MANY) {
    CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
        rows.forEachIndexed { index, row ->
            key(row.id) {
                if (index > 0) CoShellDivider()
                GoalRow(row, onClick = { onOpen(row.id) }, meter = meter)
            }
        }
    }
}

/** From this many goals the list drops the meters (board `GoalMany`): the meter stays in the goal's sheet. */
private const val MANY = 10
