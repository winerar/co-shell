package app.coshell.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.components.FeedbackResultCard
import app.coshell.feedback.components.SendButton
import app.coshell.core.designsystem.R as DesignR

/** "More → Feedback": one text field; version and phone model are added automatically. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(state: FeedbackUiState, actions: FeedbackActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feedback_title)) },
                navigationIcon = {
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState()).padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Text(
                stringResource(R.string.feedback_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CoShellTextField(
                value = state.text,
                onValueChange = actions.onTextChange,
                label = stringResource(R.string.feedback_text_label),
                hint = stringResource(R.string.feedback_counter, state.text.trim().length, SendFeedbackUseCase.MAX_LENGTH),
                isError = state.text.trim().length > SendFeedbackUseCase.MAX_LENGTH,
                enabled = !state.isSending,
                singleLine = false,
                minLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            SendButton(enabled = state.canSend, onClick = actions.onSend)
            if (state.isSending) CoShellLinearProgress()
            state.result?.let { FeedbackResultCard(it) }
        }
    }
}
