package app.coshell.core.domain.settings

import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.Flow

/** The two looks of the converter sheet (#319, board `Conv2*`): [PAIR] — «Отдаю / Получаю», [ALL] — every currency of the totals in a list. */
enum class ConverterMode { PAIR, ALL }

/**
 * What the converter remembers between openings (owner 2026-11-18): the last [mode] and the last pair — [from] is also the
 * source row of [ConverterMode.ALL]. `null` — never chosen: the main currency and the first other currency of the list.
 */
data class ConverterPick(
    val mode: ConverterMode = ConverterMode.PAIR,
    val from: CurrencyCode? = null,
    val to: CurrencyCode? = null,
)

/** The converter's last choices, on this phone only (not synced, not in the backup). */
interface ConverterSettings {
    val pick: Flow<ConverterPick>
    suspend fun update(change: (ConverterPick) -> ConverterPick)
}
