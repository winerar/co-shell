package app.coshell.wipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.DataWipeRepository
import app.coshell.core.domain.WipeAllDataUseCase
import app.coshell.wipe.WipeDataUiState.Step
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Counts what will go, waits for the word, saves a copy, wipes for the whole family. */
@HiltViewModel
class WipeDataViewModel @Inject constructor(
    private val repository: DataWipeRepository,
    private val wipeAll: WipeAllDataUseCase,
    private val backup: WipeBackup,
) : ViewModel() {

    private val _state = MutableStateFlow(WipeDataUiState())
    val state: StateFlow<WipeDataUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.update { it.copy(counts = repository.counts()) } }
    }

    fun onWordChange(word: String) = _state.update { it.copy(word = word, backupFailed = false) }

    fun wipe() {
        if (!_state.value.canWipe) return
        _state.update { it.copy(step = Step.WIPING, backupFailed = false) }
        viewModelScope.launch {
            var name: String? = null
            val done = wipeAll { backup.save().also { name = it } != null }
            _state.update {
                if (done) it.copy(step = Step.DONE, backupName = name) else it.copy(step = Step.CONFIRM, backupFailed = true)
            }
        }
    }
}
