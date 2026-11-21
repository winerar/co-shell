package app.coshell.drafts.notes

/** The note: its [text], how many lines (= expenses) it holds, and whether «Обработать» is running. */
data class NotesUiState(
    val text: String = "",
    val lineCount: Int = 0,
    val processing: Boolean = false,
    val isLoading: Boolean = false,
)
