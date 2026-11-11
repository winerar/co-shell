package app.coshell.templates.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.templates.edit.components.ChoiceField
import app.coshell.templates.edit.components.TemplatePickers
import app.coshell.templates.edit.components.TemplatePicker
import app.coshell.transactions.edit.components.TypeSelector
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteButton
import app.coshell.ui.top.ScreenTopBar

/**
 * Template editor (mockup `TplEdit`): type, name, account (for a transfer — both), amount that may stay
 * empty, category, note; «Сохранить» and, for an existing template, «Удалить».
 */
@Composable
fun TemplateEditScreen(state: TemplateEditUiState, actions: TemplateEditActions) {
    var picker by rememberSaveable { mutableStateOf<TemplatePicker?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(if (state.isNew) R.string.template_new else R.string.template_edit_title), onBack = actions.onBack)
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            TypeSelector(state.type, actions.onTypeChange)
            CoShellTextField(
                value = state.name,
                onValueChange = actions.onNameChange,
                label = stringResource(R.string.template_name),
                error = stringResource(R.string.template_name_error).takeIf { state.nameError },
            )
            ChoiceField(
                label = stringResource(if (state.isTransfer) R.string.template_from_account else R.string.template_account),
                value = state.account?.let { stringResource(R.string.template_account_value, it.name, it.currency) },
                placeholder = stringResource(R.string.template_choose_account),
                isError = state.accountError,
                onClick = { picker = TemplatePicker.ACCOUNT },
            )
            if (state.isTransfer) {
                ChoiceField(
                    label = stringResource(R.string.template_to_account),
                    value = state.toAccount?.let { stringResource(R.string.template_account_value, it.name, it.currency) },
                    placeholder = stringResource(R.string.template_choose_account),
                    isError = state.toAccountError,
                    onClick = { picker = TemplatePicker.TO_ACCOUNT },
                )
            }
            CoShellAmountField(
                value = state.amount,
                onValueChange = actions.onAmountChange,
                label = stringResource(R.string.template_amount),
                currency = state.account?.currency,
                error = stringResource(R.string.template_amount_error).takeIf { state.amountError },
                hint = stringResource(R.string.template_amount_hint),
            )
            if (!state.isTransfer) {
                ChoiceField(
                    label = stringResource(R.string.template_category),
                    value = state.category?.label,
                    placeholder = stringResource(R.string.template_category_none),
                    isError = false,
                    onClick = { picker = TemplatePicker.CATEGORY },
                )
            }
            CoShellTextField(
                value = state.note,
                onValueChange = actions.onNoteChange,
                label = stringResource(R.string.transaction_note),
                singleLine = false,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            CoShellButton(onClick = actions.onSave, modifier = Modifier.fillMaxWidth().padding(top = Spacing.s)) {
                Text(stringResource(R.string.action_save))
            }
            if (!state.isNew) DeleteButton(onClick = { confirmDelete = true })
        }
    }
    TemplatePickers(picker, state, actions, onClose = { picker = null })
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = R.string.template_delete_confirm_title,
            text = R.string.template_delete_confirm_text,
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
