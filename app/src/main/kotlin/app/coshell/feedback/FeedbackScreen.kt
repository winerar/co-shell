package app.coshell.feedback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import android.os.Build
import app.coshell.BuildConfig
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.components.FeedbackResultCard
import app.coshell.feedback.components.SendButton
import app.coshell.feedback.components.SentList
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/**
 * «Обратная связь» (frequent while the app is young): what it's about (bug / idea / design / other), the
 * text with a fill bar, «Отправить» under the thumb; after sending — a big check, then the sent list.
 */
@Composable
fun FeedbackScreen(state: FeedbackUiState, actions: FeedbackActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.feedback_title), onBack = actions.onBack)
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                AnimatedContent(
                    targetState = state.justSent,
                    transitionSpec = { (fadeIn(Motion.effects()) + scaleIn(Motion.bouncy(), initialScale = 0.9f)) togetherWith fadeOut(Motion.effectsFast()) },
                    label = "feedbackSent",
                ) { sent ->
                    if (sent) Sent(actions.onWriteMore) else Form(state, actions)
                }
                SentList(state.history, onOpen = actions.onOpen, onRemove = actions.onRemove)
            }
            if (!state.justSent) {
                Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    if (state.isSending) CoShellLinearProgress(modifier = Modifier.fillMaxWidth())
                    val label = stringResource(state.kind.label)
                    SendButton(enabled = state.canSend, onClick = { actions.onSend(label) })
                }
            }
        }
    }
}

@Composable
private fun Form(state: FeedbackUiState, actions: FeedbackActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            FeedbackKind.entries.forEach { kind ->
                CoShellFilterChip(
                    selected = state.kind == kind,
                    onClick = { actions.onKind(kind) },
                    label = { Text(stringResource(kind.label)) },
                )
            }
        }
        val length = state.text.trim().length
        CoShellTextField(
            value = state.text,
            onValueChange = actions.onTextChange,
            label = stringResource(state.kind.prompt),
            hint = stringResource(R.string.feedback_counter, length, SendFeedbackUseCase.MAX_LENGTH),
            isError = length > SendFeedbackUseCase.MAX_LENGTH,
            enabled = !state.isSending,
            singleLine = false,
            minLines = 6,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
        CoShellLinearProgress(
            progress = { (length.toFloat() / SendFeedbackUseCase.MAX_LENGTH).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.feedback_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.feedback_attached, BuildConfig.VERSION_CODE, deviceName(), Build.VERSION.RELEASE),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.result?.let { FeedbackResultCard(it) }
    }
}

@Composable
private fun Sent(onWriteMore: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Box(
            Modifier.size(Sizes.heroRing * 0.7f).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(DesignR.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(Sizes.heroIcon),
            )
        }
        Text(stringResource(R.string.feedback_result_sent), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.feedback_sent_explain),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CoShellTextButton(onClick = onWriteMore) { Text(stringResource(R.string.feedback_write_more)) }
    }
}

private fun deviceName() = "${Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }} ${Build.MODEL.orEmpty()}".trim()
