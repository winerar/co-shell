package app.coshell.settings

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ConverterMode
import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.domain.settings.ConverterSettings
import app.coshell.core.model.CurrencyCode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ConverterSettings] in the small «settings» SharedPreferences file (the converter's last mode and pair; a device preference, not in the backup). */
@Singleton
class PrefsConverterSettings @Inject constructor(@ApplicationContext context: Context) : ConverterSettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<ConverterPick?>(null)

    override val pick: Flow<ConverterPick> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { read() }) }
        .filterNotNull()

    override suspend fun update(change: (ConverterPick) -> ConverterPick) {
        val next = change(state.value ?: io { read() })
        state.value = next
        io { write(next) }
    }

    private fun read() = ConverterPick(
        mode = ConverterMode.entries.firstOrNull { it.name == prefs.getString(MODE, null) } ?: ConverterMode.PAIR,
        from = prefs.getString(FROM, null)?.takeIf { it.isNotBlank() }?.let(::CurrencyCode),
        to = prefs.getString(TO, null)?.takeIf { it.isNotBlank() }?.let(::CurrencyCode),
    )

    private fun write(pick: ConverterPick) = prefs.edit {
        putString(MODE, pick.mode.name)
        putString(FROM, pick.from?.value.orEmpty())
        putString(TO, pick.to?.value.orEmpty())
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val MODE = "converter_mode"
        const val FROM = "converter_from"
        const val TO = "converter_to"
    }
}
