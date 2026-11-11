package app.coshell.transactions.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.templates.components.TemplateStrip
import app.coshell.templates.strip.TemplateStripUiState
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.components.AccountPicker
import app.coshell.transactions.edit.components.DetailsStep
import app.coshell.transactions.edit.components.TypeSelector
import app.coshell.ui.sheet.EditSheetLayout

/**
 * Entry sheet in two steps (owner 2026-11-09): 1) type and account — «откуда списали / куда пришло»,
 * for a transfer then «куда»; 2) amount on the own keypad, category, date. Steps slide sideways in the
 * direction of travel, the sheet resizes with the same spring. An edited operation opens on step 2.
 */
@Composable
fun TransactionEditScreen(
    state: TransactionEditUiState,
    actions: TransactionEditActions,
    templates: TemplateStripUiState = TemplateStripUiState(),
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxWidth().height(Sizes.heroRing))
        return
    }
    if (state.accounts.isEmpty()) {
        EditSheetLayout(title = stringResource(R.string.transaction_edit_title_new), onSave = {}, showSave = false) {
            Text(
                stringResource(R.string.transaction_no_accounts),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CoShellButton(onClick = actions.onAddAccount) { Text(stringResource(R.string.accounts_add)) }
        }
        return
    }
    AnimatedContent(
        targetState = state.step,
        transitionSpec = { stepTransition() },
        label = "entryStep",
    ) { step ->
        when (step) {
            EntryStep.ACCOUNT -> Column {
                TypeSelector(state.type, actions.onTypeChange, Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.m))
                // Templates go over the accounts (owner picked А, #166); only a new operation has them.
                if (state.isNew && !state.isCopy) {
                    TemplateStrip(
                        items = templates.items,
                        total = templates.total,
                        onPick = actions.onTemplatePick,
                        onAll = actions.onAllTemplates,
                        onNew = actions.onNewTemplate,
                        modifier = Modifier.padding(bottom = Spacing.m),
                    )
                }
                AccountPicker(
                    title = stringResource(fromTitle(state.type)),
                    accounts = state.accounts,
                    recentIds = state.recentAccountIds,
                    selectedId = state.accountId,
                    onSelect = actions.onAccountSelect,
                )
            }
            EntryStep.TO_ACCOUNT -> AccountPicker(
                title = stringResource(R.string.picker_to_transfer),
                accounts = state.accounts,
                recentIds = state.recentAccountIds,
                selectedId = state.toAccountId,
                onSelect = actions.onToAccountSelect,
                excludeId = state.accountId,
            )
            EntryStep.DETAILS -> DetailsStep(state, actions)
        }
    }
}

private fun fromTitle(type: TransactionType): Int = when (type) {
    TransactionType.EXPENSE -> R.string.picker_from_expense
    TransactionType.INCOME -> R.string.picker_to_income
    TransactionType.TRANSFER -> R.string.picker_from_transfer
}

/** Forward — the new step comes from the right; back — from the left. A third of the width, plus a fade. */
private fun AnimatedContentTransitionScope<EntryStep>.stepTransition(): ContentTransform {
    val forward = targetState.ordinal > initialState.ordinal
    val enter = slideInHorizontally(Motion.Placement) { if (forward) it / 3 else -it / 3 } + fadeIn(Motion.effects())
    val exit = slideOutHorizontally(Motion.Placement) { if (forward) -it / 3 else it / 3 } + fadeOut(Motion.effectsFast())
    return (enter togetherWith exit).using(SizeTransform(clip = false) { _, _ -> Motion.Resize })
}
