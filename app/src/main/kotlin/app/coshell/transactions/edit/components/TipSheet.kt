package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellChoicePill
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.quickentry.TipPrefs
import app.coshell.core.domain.quickentry.Tips
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.categoryChoiceOptions
import app.coshell.transactions.edit.tip.NEW_TIP_CATEGORY
import app.coshell.transactions.edit.tip.TipUi
import app.coshell.ui.form.ChoiceField
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.sheet.ModalSheet

/**
 * «Чаевые» over the entry sheet (#322, owner 2026-11-17 А, boards `Fb322Tips`, `Fb322First`): 10 % / 15 % / «Своя сумма»
 * (typed on the keypad), the account the tip is paid from and its category. «Сохранить две операции» saves the purchase and
 * the tip as a separate expense of the same date. The category is asked the first time, in the sheet over this one.
 */
@Composable
internal fun TipSheet(state: TransactionEditUiState, tip: TipUi, actions: TransactionEditActions) {
    ModalSheet(
        title = stringResource(R.string.tip_title),
        subtitle = stringResource(R.string.tip_base, tip.baseText),
        onClosed = actions.onTipClose,
    ) {
        TipSheetBody(state, tip, actions)
    }
    if (tip.asking) TipCategorySheet(state, tip, actions)
    if (tip.pickingAccount) TipAccountSheet(state, tip, actions)
}

/** Inside of [TipSheet]; apart for the screenshot test (a sheet in a `Dialog` is not captured). */
@Composable
internal fun TipSheetBody(state: TransactionEditUiState, tip: TipUi, actions: TransactionEditActions) {
    val note = stringResource(R.string.tip_note)
    val save = { actions.onTipSave(note) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Tips.PERCENTS.forEach { percent ->
                CoShellChoicePill(
                    stringResource(R.string.tip_percent, percent),
                    selected = tip.percent == percent,
                    onClick = { actions.onTipChange { it.copy(percent = percent) } },
                )
            }
            CoShellChoicePill(
                stringResource(R.string.tip_own),
                selected = tip.percent == null,
                onClick = { actions.onTipChange { it.copy(percent = null) } },
            )
        }
        TipAmount(tip)
        ChoiceField(
            label = stringResource(R.string.tip_account),
            value = state.accounts.firstOrNull { it.id == tip.accountId }?.name,
            placeholder = "",
            isError = tip.noRate,
            onClick = { actions.onTipChange { it.copy(pickingAccount = true) } },
        )
        ChoiceField(
            label = stringResource(R.string.tip_category),
            value = tipCategoryName(state, tip),
            placeholder = stringResource(R.string.tip_category_choose),
            isError = false,
            onClick = { actions.onTipChange { it.copy(asking = true) } },
        )
        if (tip.failed) TipMessage(stringResource(R.string.tip_failed), error = true)
        if (tip.percent == null) {
            CoShellKeypad(
                onKey = { key -> actions.onTipKey(key, note) },
                doneLabel = stringResource(R.string.keypad_done),
                backspaceLabel = stringResource(R.string.keypad_backspace),
                doneEnabled = tip.canSave,
                showPlus = false,
                showDecimal = true,
            )
        } else {
            CoShellButton(onClick = save, enabled = tip.canSave, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tip_save)) }
        }
    }
}

/** The tip in the receipt currency, big, with what it comes to in the account's currency under it. */
@Composable
private fun TipAmount(tip: TipUi) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            tip.tipText.ifEmpty { stringResource(R.string.tip_amount_empty) },
            style = MaterialTheme.typography.headlineMedium,
            color = if (tip.tipText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else LocalAmountPalette.current.expense,
            maxLines = 1,
        )
        when {
            tip.noRate -> TipMessage(stringResource(R.string.tip_no_rate), error = true)
            tip.convertedText != null -> TipMessage(stringResource(R.string.tip_converted, tip.convertedText))
        }
    }
}

@Composable
private fun TipMessage(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** The name on the «Категория» row: «Как у покупки», the chosen category, or `null` — not chosen yet. */
@Composable
private fun tipCategoryName(state: TransactionEditUiState, tip: TipUi): String? = when (val id = tip.categoryId) {
    null -> null
    TipPrefs.SAME_AS_PURCHASE -> stringResource(R.string.tip_category_same)
    else -> state.categories.firstNotNullOfOrNull { g -> (listOf(g.item) + g.children).firstOrNull { it.id == id } }?.name
}

/** «Куда записывать чаевые?» (board `Fb322First`): asked once, changed from the «Категория» row. A tap picks and closes. */
@Composable
private fun TipCategorySheet(state: TransactionEditUiState, tip: TipUi, actions: TransactionEditActions) {
    val noGroup = stringResource(R.string.transaction_categories_no_folder)
    val name = stringResource(R.string.tip_note)
    val sameLabel = stringResource(R.string.tip_category_same)
    val sameHint = stringResource(R.string.tip_category_same_hint)
    val newHint = stringResource(R.string.tip_category_new_hint)
    val options = remember(state.categories, noGroup, name, sameLabel, sameHint, newHint) {
        val exists = state.categories.any { g -> (listOf(g.item) + g.children).any { it.name.trim().equals(name, ignoreCase = true) } }
        listOfNotNull(
            ChoiceOption(TipPrefs.SAME_AS_PURCHASE, sameLabel, sameHint, group = null, hasBadge = false),
            ChoiceOption(NEW_TIP_CATEGORY, name, newHint, group = null, hasBadge = false).takeUnless { exists },
        ) + categoryChoiceOptions(state.categories, noGroup)
    }
    ChoiceSheet(
        title = stringResource(R.string.tip_category_title),
        hint = stringResource(R.string.tip_category_hint),
        options = options,
        selected = tip.categoryId,
        onPick = { choice -> actions.onTipCategory(choice, name) },
        onDismiss = { actions.onTipChange { it.copy(asking = false) } },
    )
}

/** The account the tip is paid from (card for the purchase, cash for the tip). A tap picks and closes. */
@Composable
private fun TipAccountSheet(state: TransactionEditUiState, tip: TipUi, actions: TransactionEditActions) {
    val options = remember(state.accounts) {
        state.accounts.map { ChoiceOption(it.id, it.name, listOf(it.currency, it.balance).filter(String::isNotEmpty).joinToString(" · ")) }
    }
    ChoiceSheet(
        title = stringResource(R.string.tip_account),
        options = options,
        selected = tip.accountId,
        onPick = { id -> actions.onTipChange { it.copy(accountId = id, pickingAccount = false) } },
        onDismiss = { actions.onTipChange { it.copy(pickingAccount = false) } },
    )
}
