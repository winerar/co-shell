package app.coshell.feedback

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    receiptSample: Boolean = false,
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(prefill) { prefill?.let(viewModel::prefill) }
    LaunchedEffect(receiptSample) { if (receiptSample) viewModel.prefillReceiptSample() }
    // The system file picker (#561): PDF, pictures and text — the receipt formats the app can learn.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.onPick(uris.map { it.toString() })
    }
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
            onAttach = { picker.launch(FILE_TYPES) },
            onRemoveFile = viewModel::onRemoveFile,
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
    val onAttach: () -> Unit = {},
    val onRemoveFile: (String) -> Unit = {},
)

private val FILE_TYPES = arrayOf("application/pdf", "image/*", "text/plain")
