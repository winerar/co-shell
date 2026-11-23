package app.coshell.drafts.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.ai.cloud.CloudLimitNotice
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.drafts.ProcessQuickNoteUseCase
import app.coshell.core.domain.drafts.QuickNoteDrafts
import app.coshell.drafts.NoteStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * «Быстрые заметки» (#586): the text is kept as it is typed; «Обработать» turns each line into a draft, keeps in the note only the
 * lines that are not about money (#630) and says [done] so the screen moves on to «Черновики» (two steps, owner poll 2026-11-21).
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val store: NoteStore,
    private val process: ProcessQuickNoteUseCase,
    model: LanguageModel,
    private val limitNotice: CloudLimitNotice,
) : ViewModel() {
    private val cloud = model.cheap
    private val _state = MutableStateFlow(NotesUiState(isLoading = true, cloud = cloud))
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    /** One event per processed note. */
    val done: Flow<Unit> = _done.receiveAsFlow()

    init {
        viewModelScope.launch {
            val text = store.text()
            _state.value = NotesUiState(text = text, lineCount = QuickNoteDrafts.lines(text).size, cloud = cloud)
        }
    }

    fun onTextChange(text: String) {
        _state.update { it.copy(text = text, lineCount = QuickNoteDrafts.lines(text).size, skipped = 0) }
        viewModelScope.launch { store.save(text) }
    }

    fun process() {
        val current = _state.value
        if (current.isLoading || current.processing || current.lineCount == 0) return
        _state.update { it.copy(processing = true) }
        viewModelScope.launch {
            val result = runCatching { process(current.text) }.getOrNull()
            if (result == null) {
                _state.update { it.copy(processing = false) }
                return@launch
            }
            // Lines that are not about money stay in the note (#630); the rest became drafts.
            val left = result.skipped.joinToString("\n")
            store.save(left)
            if (result.limited) limitNotice.raise()
            _state.value = NotesUiState(text = left, lineCount = result.skipped.size, skipped = result.skipped.size, cloud = cloud)
            if (result.made > 0) _done.send(Unit)
        }
    }
}
