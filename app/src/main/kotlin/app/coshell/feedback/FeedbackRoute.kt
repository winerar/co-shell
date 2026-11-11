package app.coshell.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [FeedbackScreen] to its ViewModel. [prefill] — text to start with (the sync banner's error code); [onOpen] — a sent message's card. */
@Composable
fun FeedbackRoute(
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    prefill: String? = null,
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(prefill) { prefill?.let(viewModel::prefill) }
    FeedbackScreen(
        state = state,
        actions = FeedbackActions(
            onBack = onBack,
            onTextChange = viewModel::onTextChange,
            onSend = viewModel::send,
            onKind = viewModel::onKind,
            onWriteMore = viewModel::onWriteMore,
            onOpen = onOpen,
            onRemove = viewModel::remove,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class FeedbackActions(
    val onBack: () -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    /** Gets the localized name of the chosen kind, which goes in front of the text. */
    val onSend: (String) -> Unit = {},
    val onKind: (FeedbackKind) -> Unit = {},
    val onWriteMore: () -> Unit = {},
    /** A sent message by its time: opens its card. */
    val onOpen: (Long) -> Unit = {},
    val onRemove: (Long) -> Unit = {},
)
