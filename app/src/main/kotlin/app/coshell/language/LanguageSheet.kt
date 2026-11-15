package app.coshell.language

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.language.components.LanguageList

/**
 * The one-time question over «Обзор» (#352, board Fb352A): on the first launch with the phone's language picked,
 * once after the update with «Оставить русский» picked. «Готово» or a swipe away keeps what is picked — the
 * question is not asked again; the screen restarts if the language changed.
 */
@Composable
fun LanguagePromptHost(viewModel: LanguageViewModel = hiltViewModel()) {
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    if (prompt == LanguagePrompt.NONE) return
    val activity = LocalActivity.current
    val choices = remember(prompt) { LanguageChoices.forPrompt(prompt) }
    var selected by remember(prompt) { mutableStateOf(choices.first()) }
    val update = prompt == LanguagePrompt.UPDATE
    LanguageSheet(
        title = stringResource(if (update) R.string.language_update_title else R.string.language_sheet_title),
        text = stringResource(if (update) R.string.language_update_text else R.string.language_first_text),
        choices = choices,
        selected = selected,
        phoneLanguage = viewModel.phoneLanguage,
        onPick = { choice, _ -> selected = choice },
        showDone = true,
        onClosed = { viewModel.apply(selected.tag, fromPrompt = true) { activity?.recreate() } },
    )
}

/** «Внешний вид → Язык» (board Fb352Set): a tap applies at once and closes. */
@Composable
fun LanguageSettingsSheet(onClosed: () -> Unit, viewModel: LanguageViewModel = hiltViewModel()) {
    val activity = LocalActivity.current
    val chosen = remember { LanguageChoice(viewModel.chosen) }
    var picked by remember { mutableStateOf(chosen) }
    LanguageSheet(
        title = stringResource(R.string.language_sheet_title),
        text = stringResource(R.string.language_settings_text),
        choices = remember { LanguageChoices.all() },
        selected = picked,
        phoneLanguage = viewModel.phoneLanguage,
        onPick = { choice, close ->
            picked = choice
            close()
        },
        showDone = false,
        onClosed = {
            onClosed()
            if (picked != chosen) viewModel.apply(picked.tag, fromPrompt = false) { activity?.recreate() }
        },
    )
}

@Composable
private fun LanguageSheet(
    title: String,
    text: String,
    choices: List<LanguageChoice>,
    selected: LanguageChoice,
    phoneLanguage: String,
    onPick: (choice: LanguageChoice, close: () -> Unit) -> Unit,
    showDone: Boolean,
    onClosed: () -> Unit,
) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(title = title, actionLabel = stringResource(R.string.keypad_done), onAction = close, showAction = showDone) {
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LanguageList(choices, selected, phoneLanguage, AppLanguage.current()) { onPick(it, close) }
            }
        }
    }
}
