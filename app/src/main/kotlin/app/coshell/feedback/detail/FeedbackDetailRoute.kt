package app.coshell.feedback.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.BuildConfig

/** Connects [FeedbackDetailScreen] to the history; «Убрать из списка» removes and goes back. */
@Composable
fun FeedbackDetailRoute(sentAtMillis: Long, onBack: () -> Unit, viewModel: FeedbackDetailViewModel = hiltViewModel()) {
    val entry by remember(sentAtMillis) { viewModel.entry(sentAtMillis) }.collectAsStateWithLifecycle(null)
    FeedbackDetailScreen(
        entry = entry,
        currentBuild = BuildConfig.VERSION_CODE,
        onBack = onBack,
        onRemove = {
            viewModel.remove(sentAtMillis)
            onBack()
        },
    )
}
