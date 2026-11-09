package app.coshell.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.CoShellDatabaseConstructor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.ExperimentalPathApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Every schema version must migrate with data intact (docs/agents/database.md §«Миграции»). */
class MigrationTest {
    private val dir: Path = Files.createTempDirectory("coshell-migration")
    private val helper = MigrationTestHelper(
        schemaDirectoryPath = Path.of("schemas"),
        databasePath = dir.resolve("test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = CoShellDatabase::class,
        databaseFactory = { CoShellDatabaseConstructor.initialize() },
    )

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun `1 to 2 keeps data and adds rate tables`() {
        helper.createDatabase(1).use { v1 ->
            v1.execSQL(
                """INSERT INTO accounts (id, name, currency, type, initial_balance_minor, is_shared,
                   owner_member_id, archived, sort_order, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('a1', 'Cash', 'ARS', 'CASH', 5000, 1, NULL, 0, 0, 1, NULL, 0, 1)""",
            )
        }

        helper.runMigrationsAndValidate(2, emptyList()).use { v2 ->
            assertEquals("Cash|5000", v2.single("SELECT name || '|' || initial_balance_minor FROM accounts"))
            v2.execSQL(
                """INSERT INTO manual_rates (id, date, from_currency, to_currency, rate, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('2026-11-09:ARS:USD', '2026-11-09', 'USD', 'ARS', '1450.5', 1, NULL, 0, 1)""",
            )
            v2.execSQL(
                """INSERT INTO downloaded_rates (date, from_currency, to_currency, rate, source, downloaded_at)
                   VALUES ('2026-11-09', 'USD', 'AMD', '386.1', 'currency-api', 1)""",
            )
            assertEquals("1450.5", v2.single("SELECT rate FROM manual_rates"))
            assertEquals("386.1", v2.single("SELECT rate FROM downloaded_rates"))
        }
    }

    @Test
    fun `2 to 3 keeps accounts and includes them in the total`() {
        helper.createDatabase(2).use { v2 ->
            v2.execSQL(
                """INSERT INTO accounts (id, name, currency, type, initial_balance_minor, is_shared,
                   owner_member_id, archived, sort_order, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('a1', 'Cash', 'ARS', 'CASH', 5000, 1, NULL, 0, 0, 1, NULL, 0, 1)""",
            )
        }

        helper.runMigrationsAndValidate(3, emptyList()).use { v3 ->
            assertEquals("Cash|5000|0", v3.single("SELECT name || '|' || initial_balance_minor || '|' || excluded_from_total FROM accounts"))
        }
    }

    private fun SQLiteConnection.single(sql: String): String =
        prepare(sql).use { statement ->
            check(statement.step()) { "No rows: $sql" }
            statement.getText(0)
        }
}
