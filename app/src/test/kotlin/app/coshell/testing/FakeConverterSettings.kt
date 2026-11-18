package app.coshell.testing

import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.domain.settings.ConverterSettings
import kotlinx.coroutines.flow.MutableStateFlow

/** [ConverterSettings] in memory. */
class FakeConverterSettings(initial: ConverterPick = ConverterPick()) : ConverterSettings {
    val state = MutableStateFlow(initial)
    override val pick = state
    override suspend fun update(change: (ConverterPick) -> ConverterPick) {
        state.value = change(state.value)
    }
}
