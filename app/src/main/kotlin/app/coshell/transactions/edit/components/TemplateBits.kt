package app.coshell.transactions.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState

/**
 * «⚡ Испанский ✕» — the operation is being filled from a template (mockup `TplEntry`); the violet of
 * view modes, like «Копия · сегодня». [onClear] null — just a label (the template was saved right now).
 */
@Composable
internal fun TemplatePill(name: String, onClear: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    val clickable = if (onClear != null) Modifier.tactileClickable(Tactile.Control, PillShape, onClick = onClear) else Modifier
    Row(
        modifier = clickable.background(colors.secondaryContainer, PillShape).padding(horizontal = Spacing.m, vertical = Spacing.xs + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(painterResource(DesignR.drawable.ic_bolt), null, Modifier.size(Sizes.tileIcon), tint = colors.onSecondaryContainer)
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = Sizes.tileMinHeight),
        )
        if (onClear != null) {
            Icon(
                painterResource(DesignR.drawable.ic_close),
                stringResource(R.string.template_clear),
                Modifier.size(Sizes.tileIcon),
                tint = colors.onSecondaryContainer,
            )
        }
    }
}

/** «Из шаблона · сумму можно поправить» under the amount; without a remembered amount — «введите сумму». */
@Composable
internal fun templateHint(state: TransactionEditUiState): String? = when {
    state.templateName == null -> null
    state.amount.isBlank() -> stringResource(R.string.template_hint_enter_amount)
    else -> stringResource(R.string.template_hint_amount)
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
    CoShellDialog(
        title = stringResource(R.string.template_save_as_title),
        confirmLabel = stringResource(R.string.action_save),
        onConfirm = { onSave(name, rememberAmount) },
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
        confirmEnabled = name.isNotBlank(),
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
