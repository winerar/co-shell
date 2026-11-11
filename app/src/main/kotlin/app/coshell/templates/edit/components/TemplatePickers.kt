package app.coshell.templates.edit.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.templates.edit.TemplateEditActions
import app.coshell.templates.edit.TemplateEditUiState
import app.coshell.ui.form.ChoiceDialog
import app.coshell.ui.form.ChoiceOption

/** Which list the editor has open. */
enum class TemplatePicker { ACCOUNT, TO_ACCOUNT, CATEGORY }

/** A form field that opens a list: label on top, the chosen value (or [placeholder]) under it, «›». */
@Composable
internal fun ChoiceField(label: String, value: String?, placeholder: String, isError: Boolean, onClick: () -> Unit) {
    CoShellListRow(
        title = { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        supporting = {
            Text(
                value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    value == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        },
        trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
    )
}

/** The open list of [picker]: accounts (a transfer's target — without the source) or categories of the type. */
@Composable
internal fun TemplatePickers(picker: TemplatePicker?, state: TemplateEditUiState, actions: TemplateEditActions, onClose: () -> Unit) {
    when (picker) {
        null -> Unit
        TemplatePicker.ACCOUNT -> ChoiceDialog(
            title = stringResource(R.string.template_account),
            options = state.accounts.map { ChoiceOption(it.id, it.name, it.currency) },
            selected = state.accountId,
            onPick = { actions.onAccountSelect(it); onClose() },
            onDismiss = onClose,
        )
        TemplatePicker.TO_ACCOUNT -> ChoiceDialog(
            title = stringResource(R.string.template_to_account),
            options = state.accounts.filter { it.id != state.accountId }.map { ChoiceOption(it.id, it.name, it.currency) },
            selected = state.toAccountId,
            onPick = { actions.onToAccountSelect(it); onClose() },
            onDismiss = onClose,
        )
        TemplatePicker.CATEGORY -> ChoiceDialog<String?>(
            title = stringResource(R.string.template_category),
            options = listOf(ChoiceOption<String?>(null, stringResource(R.string.template_category_none))) +
                state.categoryChoices.map { ChoiceOption<String?>(it.id, it.label) },
            selected = state.categoryId,
            onPick = { actions.onCategorySelect(it); onClose() },
            onDismiss = onClose,
        )
    }
}
