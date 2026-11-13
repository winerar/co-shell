package app.coshell.update.past

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.ReleaseHistorySource
import app.coshell.core.domain.update.ReleaseNote
import app.coshell.core.domain.update.pastReleases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** «Прошлые версии» (#284): loads `releases.json` once per screen, «Повторить» after a failure. */
@HiltViewModel
class PastReleasesViewModel @Inject constructor(
    private val source: ReleaseHistorySource,
    private val installed: InstalledVersion,
) : ViewModel() {

    private val _state = MutableStateFlow<PastReleasesState>(PastReleasesState.Loading)
    val state: StateFlow<PastReleasesState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = PastReleasesState.Loading
        viewModelScope.launch {
            _state.value = try {
                PastReleasesState.Loaded(pastReleases(source.history(), installed.versionCode), installed.versionCode)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                PastReleasesState.Failed
            }
        }
    }
}

sealed interface PastReleasesState {
    data object Loading : PastReleasesState
    data object Failed : PastReleasesState
    /** [releases] newest first; empty when the history is not published yet (or updates are off in this build). */
    data class Loaded(val releases: List<ReleaseNote>, val installedVersionCode: Int) : PastReleasesState
}
