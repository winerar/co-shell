package app.coshell.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** «ИИ на телефоне» (#587): shows [AiAddonController]'s state; «Включить» first asks about the 2,5 GB (poll 2026-11-21, «А»). */
@HiltViewModel
class AiAddonViewModel @Inject constructor(
    private val controller: AiAddonController,
    memory: DeviceMemory,
) : ViewModel() {
    private val ramMb = memory.totalMb()
    private val askData = MutableStateFlow(false)

    val state: StateFlow<AiAddonUiState> = combine(controller.state, askData) { s, ask -> s.toUi(ask).forMemory(ramMb) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), controller.state.value.toUi(false).forMemory(ramMb))

    init {
        viewModelScope.launch { controller.refresh() }
    }

    fun onEnable() = askData.update { true }

    fun onAskDismiss() = askData.update { false }

    fun onConfirmDownload() {
        askData.value = false
        controller.enable()
    }

    fun onCancel() = controller.cancel()

    fun onDeleteModel() = controller.deleteModel()
}

/** The phase for the «Настройки» row and the screen alike. */
internal fun AiAddonState.toUi(askData: Boolean): AiAddonUiState = when (val p = phase) {
    AiAddonPhase.Off -> AiAddonUiState(AiAddonStage.OFF, problem = problem, askData = askData)
    is AiAddonPhase.InstallingAddon -> AiAddonUiState(AiAddonStage.STEP_ADDON, progress = p.progress)
    is AiAddonPhase.DownloadingModel -> {
        val fraction = if (p.total > 0) (p.done.toFloat() / p.total).coerceIn(0f, 1f) else null
        AiAddonUiState(AiAddonStage.STEP_MODEL, fraction, p.done / MB, p.total / MB)
    }
    AiAddonPhase.On -> AiAddonUiState(AiAddonStage.ON)
}

private const val MB = 1_000_000L

/** A phone under 6 GB cannot run the model: the OFF screen turns into «не подойдёт» (a model already on it stays usable). */
internal fun AiAddonUiState.forMemory(ramMb: Long): AiAddonUiState =
    if (stage == AiAddonStage.OFF && ramMb < DeviceMemory.MIN_MB) copy(stage = AiAddonStage.UNSUPPORTED, memoryGb = ((ramMb + 512) / 1024).toInt(), askData = false) else this
