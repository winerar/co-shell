package app.coshell.core.domain.settings

import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.Flow

/**
 * Report currencies (ARCHITECTURE §6): totals on "Overview" are shown in each of them; the first
 * one is the **main** currency (also the base of the "Rates" screen). Never empty.
 * Kept on the device for now; moves to the family settings with sync (stage 4).
 */
interface ReportCurrencySettings {
    val currencies: Flow<List<CurrencyCode>>
    suspend fun setCurrencies(currencies: List<CurrencyCode>)

    companion object {
        /** Until the family picks its own: USD, the pivot of every downloaded rate. */
        val DEFAULT: List<CurrencyCode> = listOf(CurrencyCode("USD"))
    }
}

/** Edits of the report currency list; each keeps it non-empty and without duplicates. */
object ReportCurrencies {
    /** Adds [code] at the end; already present — unchanged. */
    fun add(list: List<CurrencyCode>, code: CurrencyCode): List<CurrencyCode> =
        if (code in list) list else list + code

    /** Removes [code]; the last remaining currency cannot be removed. */
    fun remove(list: List<CurrencyCode>, code: CurrencyCode): List<CurrencyCode> =
        if (list.size <= 1) list else list - code

    /** Moves [code] to the front (adds it if missing): it becomes the main currency. */
    fun makeMain(list: List<CurrencyCode>, code: CurrencyCode): List<CurrencyCode> =
        listOf(code) + (list - code)

    /** Cleans a stored list: drops duplicates, falls back to [ReportCurrencySettings.DEFAULT] when empty. */
    fun normalize(list: List<CurrencyCode>): List<CurrencyCode> =
        list.distinct().ifEmpty { ReportCurrencySettings.DEFAULT }
}
