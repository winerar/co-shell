package app.coshell.core.data.repository

import app.coshell.core.data.db.SettingDao
import app.coshell.core.data.db.SettingEntity
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.settings.ReportCurrencies
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Family settings (#122) in the synced `settings` table: both phones see the same values, and
 * without a family they simply stay on this phone (rows wait in `pending_sync`).
 */
class RoomFamilySettings(
    private val settings: SettingDao,
    private val stamper: WriteStamper,
) : ReportCurrencySettings, MonthStartSettings {

    override val currencies: Flow<List<CurrencyCode>> =
        settings.observe(REPORT_CURRENCIES).map(ReportCurrencies::decode).distinctUntilChanged()

    override suspend fun setCurrencies(currencies: List<CurrencyCode>) =
        write(REPORT_CURRENCIES, ReportCurrencies.encode(currencies))

    override val monthStartDay: Flow<Int> =
        settings.observe(MONTH_START_DAY).map(MonthStartSettings::parse).distinctUntilChanged()

    override suspend fun setMonthStartDay(day: Int) {
        require(day in MonthStartSettings.DAYS) { "Month start day must be 1..31, got $day" }
        write(MONTH_START_DAY, day.toString())
    }

    /**
     * Brings over the report currencies this phone kept before #122. Written as the oldest possible
     * edit (`updated_at = 0`) and only if the family setting does not exist yet: whatever the family
     * already chose wins on the next sync, and of two phones with different lists the first to update wins.
     */
    suspend fun importLegacyReportCurrencies(currencies: List<CurrencyCode>) {
        val stamp = stamper.stamp()
        settings.insertIfAbsent(
            SettingEntity(REPORT_CURRENCIES, ReportCurrencies.encode(currencies), updatedAt = 0, updatedBy = stamp.by),
        )
    }

    private suspend fun write(key: String, value: String) {
        val stamp = stamper.stamp()
        settings.upsert(listOf(SettingEntity(key, value, stamp.now, stamp.by)))
    }

    companion object {
        const val REPORT_CURRENCIES = "report_currencies"
        const val MONTH_START_DAY = "month_start_day"
    }
}
