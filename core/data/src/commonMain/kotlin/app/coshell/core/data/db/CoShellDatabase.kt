package app.coshell.core.data.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Migration policy (see docs/agents/database.md): bump [version] on every schema change, commit the
 * exported schema from `core/data/schemas/`, add an auto- or manual migration and a test.
 * Destructive fallback is never enabled — user data lives only here until sync (stage 4).
 */
@Database(
    entities = [
        AccountEntity::class, CategoryEntity::class, TransactionEntity::class, BudgetEntity::class,
        ManualRateEntity::class, DownloadedRateEntity::class, SyncStateEntity::class, TemplateEntity::class,
        SettingEntity::class,
    ],
    version = 10,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2), // v2: manual_rates, downloaded_rates
        AutoMigration(from = 2, to = 3), // v3: accounts.excluded_from_total
        AutoMigration(from = 3, to = 4), // v4: sync_state
        AutoMigration(from = 4, to = 5), // v5: accounts.color
        AutoMigration(from = 5, to = 6), // v6: transactions.import_batch_id
        AutoMigration(from = 6, to = 7), // v7: templates
        AutoMigration(from = 7, to = 8), // v8: budgets.note, rollover, last_month
        AutoMigration(from = 8, to = 9), // v9: settings
        AutoMigration(from = 9, to = 10), // v10: index on transactions.pending_sync (#259)
    ],
)
@ConstructedBy(CoShellDatabaseConstructor::class)
abstract class CoShellDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun templateDao(): TemplateDao
    abstract fun settingDao(): SettingDao
    abstract fun backupDao(): BackupDao
    abstract fun rateDao(): RateDao
    abstract fun syncDao(): SyncDao
    abstract fun importDao(): ImportDao
    abstract fun wipeDao(): WipeDao
    abstract fun integrityDao(): IntegrityDao
    abstract fun reportDao(): ReportDao

    companion object {
        const val FILE_NAME = "coshell.db"
    }
}

// Room generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object CoShellDatabaseConstructor : RoomDatabaseConstructor<CoShellDatabase> {
    override fun initialize(): CoShellDatabase
}

/** Common builder settings for every platform. */
fun RoomDatabase.Builder<CoShellDatabase>.configureCommon(): CoShellDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
