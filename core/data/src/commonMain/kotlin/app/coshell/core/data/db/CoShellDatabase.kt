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
        SettingEntity::class, TagEntity::class, SyncExtraEntity::class, DraftEntity::class, DebtEventEntity::class,
        GoalEntity::class,
    ],
    version = 27,
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
        AutoMigration(from = 10, to = 11), // v11: tags (#129)
        AutoMigration(from = 11, to = 12), // v12: sync_extras — fields of a newer version (#340)
        AutoMigration(from = 12, to = 13), // v13: templates.schedule — regular payments (#143)
        AutoMigration(from = 13, to = 14), // v14: templates.account_currency — payment in another currency (#415)
        AutoMigration(from = 14, to = 15), // v15: transactions.receipt_key — scanned receipt (#526)
        AutoMigration(from = 15, to = 16), // v16: budgets.zero_limit — a zero limit (#576)
        AutoMigration(from = 16, to = 17), // v17: drafts — quick notes, device-only (#586)
        AutoMigration(from = 17, to = 18), // v18: accounts.card_digits — a bank receipt finds its account (#577)
        AutoMigration(from = 18, to = 19), // v19: accounts.prepaid — a travel pass (#645)
        AutoMigration(from = 19, to = 20), // v20: accounts.credit_* — a credit card (#686)
        AutoMigration(from = 20, to = 21), // v21: debt_events — debts and loans (#552)
        AutoMigration(from = 23, to = 24), // v24: accounts.deposit_start — the day a deposit was opened (#634)
        AutoMigration(from = 22, to = 23), // v23: accounts.deposit_rate_bp, deposit_end, deposit_payout — deposit conditions (#634)
        AutoMigration(from = 21, to = 22), // v22: accounts.credit_statement_day, credit_grace_days — grace period of a credit card (#634)
        AutoMigration(from = 24, to = 25), // v25: accounts.local_only — «Только у меня» (#558)
        AutoMigration(from = 25, to = 26), // v26: goals — savings goals (#768)
        AutoMigration(from = 26, to = 27), // v27: templates.labels — tags a template remembers (#296)
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
    abstract fun tagDao(): TagDao
    abstract fun debtDao(): DebtDao
    abstract fun goalDao(): GoalDao
    abstract fun backupDao(): BackupDao
    abstract fun rateDao(): RateDao
    abstract fun syncDao(): SyncDao
    abstract fun importDao(): ImportDao
    abstract fun wipeDao(): WipeDao
    abstract fun integrityDao(): IntegrityDao
    abstract fun reportDao(): ReportDao
    abstract fun draftDao(): DraftDao

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
