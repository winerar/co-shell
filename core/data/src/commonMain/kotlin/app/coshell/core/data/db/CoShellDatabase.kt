package app.coshell.core.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Bump [version] on every schema change and add a migration (auto or manual) — never
 * destructive fallback: this is the family's only local copy of the data. See docs/agents/database.md.
 */
@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(CoShellDatabaseConstructor::class)
abstract class CoShellDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "coshell.db"
    }
}

// Room generates the actual implementations for every target.
@Suppress("KotlinNoActualForExpect")
expect object CoShellDatabaseConstructor : RoomDatabaseConstructor<CoShellDatabase> {
    override fun initialize(): CoShellDatabase
}
