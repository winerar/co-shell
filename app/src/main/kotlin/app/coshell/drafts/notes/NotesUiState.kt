package app.coshell.drafts.notes

/**
 * The note: its [text], how many lines (= expenses) it holds, and whether «Обработать» is running. [skipped] — lines the last
 * «Обработать» left in the note as not about money (#630); 0 once the text is edited.
 */
data class NotesUiState(
    val text: String = "",
    val lineCount: Int = 0,
    val processing: Boolean = false,
    val isLoading: Boolean = false,
    val skipped: Int = 0,
)
