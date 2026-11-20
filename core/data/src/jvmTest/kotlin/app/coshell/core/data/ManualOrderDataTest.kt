package app.coshell.core.data

import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomFamilySettings
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** #338: lists go alphabetically until the family drags them; then by `sortOrder`. */
class ManualOrderDataTest {
    private val db = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper()
    private val settings = RoomFamilySettings(db.settingDao(), stamper)
    private val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper, db.settingDao())
    private val categories = RoomCategoryRepository(db.categoryDao(), stamper, db.settingDao())
    private val usd = CurrencyCode("USD")

    @AfterTest
    fun close() = db.close()

    @Test
    fun accountsSwitchFromAlphabetToManualOrder() = runTest {
        accounts.saveAccounts(listOf(Account("b", "Banco", usd, sortOrder = 1), Account("a", "Cash", usd, sortOrder = 2), Account("c", "Alfa", usd, sortOrder = 3)))
        assertEquals(listOf("c", "b", "a"), accounts.observeAccounts().first().map { it.id })
        settings.setManualAccounts()
        assertEquals(listOf("b", "a", "c"), accounts.observeAccounts().first().map { it.id })
        assertEquals(true, settings.manualAccounts.first())
    }

    @Test
    fun categoriesSwitchToManualOrder() = runTest {
        categories.saveCategories(
            listOf(Category("t", "Такси", CategoryType.EXPENSE, sortOrder = 1), Category("a", "Аптека", CategoryType.EXPENSE, sortOrder = 2)),
        )
        assertEquals(listOf("a", "t"), categories.observeCategories().first().map { it.id })
        settings.setManualCategories()
        assertEquals(listOf("t", "a"), categories.observeCategories().first().map { it.id })
        assertEquals(false, settings.manualAccounts.first())
    }
}
