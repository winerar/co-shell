package app.coshell.drafts.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.top.ScreenTopBar

/** What the screen can ask for; defaults keep previews short. */
data class NotesActions(
    val onBack: () -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    val onProcess: () -> Unit = {},
)

/**
 * «Быстрые заметки» (#586, board `Notes586Input`): one field, a line per expense («кофе 3500», «вчера аптека 4200»);
 * «Обработать · N» below makes drafts of them.
 */
@Composable
fun NotesScreen(state: NotesUiState, actions: NotesActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.notes_title), onBack = actions.onBack) },
        bottomBar = {
            val label = if (state.lineCount > 0) stringResource(R.string.notes_process, state.lineCount) else stringResource(R.string.notes_process_none)
            Column(
                Modifier.fillMaxWidth().aboveBottomBars().padding(horizontal = Spacing.l, vertical = Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CoShellButton(
                    onClick = actions.onProcess,
                    enabled = state.lineCount > 0 && !state.processing && !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.processing) stringResource(R.string.notes_processing) else label) }
            }
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(underBars(padding)).padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            CoShellTextField(
                value = state.text,
                onValueChange = actions.onTextChange,
                label = stringResource(R.string.notes_label),
                placeholder = stringResource(R.string.notes_placeholder),
                singleLine = false,
                minLines = MIN_LINES,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Default),
                modifier = Modifier.fillMaxWidth(),
            )
            val hint = when {
                state.skipped > 0 -> pluralStringResource(R.plurals.notes_skipped, state.skipped, state.skipped)
                state.text.isBlank() -> stringResource(R.string.notes_empty_hint)
                else -> stringResource(R.string.notes_hint)
            }
            CoShellHint(hint, Modifier.padding(horizontal = Spacing.s))
        }
    }
}

private const val MIN_LINES = 7
