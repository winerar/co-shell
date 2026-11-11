package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.SettingEntity
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomFamilySettings
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FamilySettingsTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val db = createInMemoryCoShellDatabase()
    private val settings = RoomFamilySettings(db.settingDao(), WriteStamper())

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun `defaults before anything is chosen`() = runTest {
        assertEquals(ReportCurrencySettings.DEFAULT, settings.currencies.first())
        assertEquals(1, settings.monthStartDay.first())
    }

    @Test
    fun `chosen values are read back and wait to be sent`() = runTest {
        settings.setCurrencies(listOf(ars, usd, ars))
        settings.setMonthStartDay(10)

        assertEquals(listOf(ars, usd), settings.currencies.first(), "duplicates dropped")
        assertEquals(10, settings.monthStartDay.first())
        assertEquals(2, db.syncDao().pending().settings.size)
    }

    @Test
    fun `month start day outside 1 to 31 is refused`() = runTest {
        assertFailsWith<IllegalArgumentException> { settings.setMonthStartDay(0) }
        assertFailsWith<IllegalArgumentException> { settings.setMonthStartDay(32) }
        assertEquals(1, settings.monthStartDay.first())
    }

    @Test
    fun `broken stored values fall back to defaults`() = runTest {
        db.settingDao().upsert(
            listOf(
                SettingEntity(RoomFamilySettings.REPORT_CURRENCIES, "??, ,", 1, null),
                SettingEntity(RoomFamilySettings.MONTH_START_DAY, "40", 1, null),
            ),
        )
        assertEquals(ReportCurrencySettings.DEFAULT, settings.currencies.first())
        assertEquals(1, settings.monthStartDay.first())
    }

    @Test
    fun `legacy list moves in once and never overwrites a family choice`() = runTest {
        settings.importLegacyReportCurrencies(listOf(ars, usd))
        assertEquals(listOf(ars, usd), settings.currencies.first())

        settings.setCurrencies(listOf(amd))
        settings.importLegacyReportCurrencies(listOf(usd))
        assertEquals(listOf(amd), settings.currencies.first())
    }

    @Test
    fun `settings travel in the backup`() = runTest {
        settings.setCurrencies(listOf(usd, ars))
        settings.setMonthStartDay(5)
        val json = JsonBackupRepository(db, WriteStamper()).exportJson()

        val other = createInMemoryCoShellDatabase()
        try {
            assertIs<BackupImportResult.Imported>(JsonBackupRepository(other, WriteStamper()).importJson(json))
            val restored = RoomFamilySettings(other.settingDao(), WriteStamper())
            assertEquals(listOf(usd, ars), restored.currencies.first())
            assertEquals(5, restored.monthStartDay.first())
        } finally {
            other.close()
        }
    }
}
