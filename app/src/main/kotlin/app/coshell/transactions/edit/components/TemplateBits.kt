package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.ui.sheet.ModalSheet

/**
 * «⚡ Испанский ✕» — the operation is being filled from a template (mockup `TplEntry`); the violet of
 * view modes, like «Копия · сегодня» (poll #367 А). [onClear] null — just a label (the template was saved right now).
 * A payment from «Скоро» shows «⟳ Свет · 15 нояб.» with [icon] `ic_refresh` (board `Rec143Post`).
 */
@Composable
internal fun TemplatePill(name: String, onClear: (() -> Unit)?, icon: Int = DesignR.drawable.ic_bolt) {
    if (onClear == null) {
        CoShellStatusChip(name, StatusTone.Manual)
        return
    }
    // A plain choice chip, not chosen (poll #367 А): the ✕ takes the template off.
    CoShellFilterChip(
        selected = false,
        onClick = onClear,
        label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = Sizes.tileMinHeight)) },
        leadingIcon = { Icon(painterResource(icon), null, Modifier.size(Sizes.chipIcon)) },
        trailingIcon = { Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.template_clear), Modifier.size(Sizes.chipIcon)) },
    )
}

/**
 * «Из шаблона · сумму можно поправить» under the amount; without a remembered amount — «введите сумму».
 * A payment paid before says how much it was: «В прошлый раз 41 300 · в среднем 44 000» (#404).
 */
@Composable
internal fun templateHint(state: TransactionEditUiState): String? {
    val last = state.payment?.last
    val average = state.payment?.average
    val account = state.account
    return when {
        state.templateName == null -> null
        // A payment in another currency without a rate (#415, board `Rec415NoRate`): the user enters what was debited.
        state.payment != null && state.hasReceipt && state.amount.isBlank() && account != null ->
            stringResource(R.string.payment_hint_no_rate, state.receiptCurrency, account.currency)
        last != null && average != null -> stringResource(R.string.payment_hint_last_average, last, average)
        last != null -> stringResource(R.string.payment_hint_last, last)
        state.amount.isBlank() -> stringResource(R.string.template_hint_enter_amount)
        else -> stringResource(R.string.template_hint_amount)
    }
}

/**
 * «Сохранить как шаблон» over the editor of a saved operation (mockup `TplSave`): a name and whether to
 * remember the amount. Closes itself once the template is saved.
 */
@Composable
internal fun SaveAsTemplateDialog(
    state: TransactionEditUiState,
    onSave: (name: String, rememberAmount: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val categoryName = state.categories.flatMap { listOf(it.item) + it.children }.firstOrNull { it.id == state.categoryId }?.name
    var name by rememberSaveable { mutableStateOf(state.note.lineSequence().firstOrNull().orEmpty().ifBlank { categoryName.orEmpty() }) }
    var rememberAmount by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.savedTemplateName) { if (state.savedTemplateName != null) onDismiss() }
    ModalSheet(
        title = stringResource(R.string.template_save_as_title),
        actionLabel = stringResource(R.string.action_save),
        onAction = { onSave(name, rememberAmount) },
        onClosed = onDismiss,
        actionEnabled = name.isNotBlank(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Text(
                rememberedText(state, categoryName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CoShellTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.template_name),
                error = stringResource(R.string.template_save_failed).takeIf { state.templateSaveFailed },
            )
            CoShellSwitchRow(
                title = stringResource(R.string.template_remember_amount),
                checked = rememberAmount,
                onCheckedChange = { rememberAmount = it },
                subtitle = stringResource(R.string.template_remember_amount_hint, state.amount, state.account?.currency.orEmpty()),
            )
        }
    }
}

@Composable
private fun rememberedText(state: TransactionEditUiState, categoryName: String?): String {
    val account = state.account?.name.orEmpty()
    return when {
        state.isTransfer -> stringResource(R.string.template_remembers_transfer, account, state.toAccount?.name.orEmpty())
        categoryName != null -> stringResource(R.string.template_remembers, account, categoryName)
        else -> stringResource(R.string.template_remembers_no_category, account)
    }
}
