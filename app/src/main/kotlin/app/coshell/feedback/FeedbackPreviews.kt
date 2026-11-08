package app.coshell.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.feedback.FeedbackResult

@Preview(showBackground = true)
@Composable
private fun FeedbackEmptyPreview() {
    CoShellTheme { FeedbackScreen(FeedbackUiState(), FeedbackActions()) }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackSentPreview() {
    CoShellTheme { FeedbackScreen(FeedbackUiState(result = FeedbackResult.Sent), FeedbackActions()) }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackFailedPreview() {
    CoShellTheme {
        FeedbackScreen(FeedbackUiState(text = "Не сохраняется заметка", result = FeedbackResult.Failed("HTTP 403")), FeedbackActions())
    }
}
