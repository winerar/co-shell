package app.coshell.core.data.db

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
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(CoShellDatabaseConstructor::class)
abstract class CoShellDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun backupDao(): BackupDao

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
