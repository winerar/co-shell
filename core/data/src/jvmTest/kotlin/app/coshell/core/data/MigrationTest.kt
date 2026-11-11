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

    @Test
    fun `3 to 4 keeps pending rows and adds sync state`() {
        helper.createDatabase(3).use { v3 ->
            v3.execSQL(
                """INSERT INTO accounts (id, name, currency, type, initial_balance_minor, is_shared,
                   owner_member_id, archived, sort_order, excluded_from_total, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('a1', 'Cash', 'ARS', 'CASH', 5000, 1, NULL, 0, 0, 0, 1, NULL, 0, 1)""",
            )
        }

        helper.runMigrationsAndValidate(4, emptyList()).use { v4 ->
            // Existing rows stay pending, so the first sync sends this phone's data to the family.
            assertEquals("Cash|1", v4.single("SELECT name || '|' || pending_sync FROM accounts"))
            assertEquals("0", v4.single("SELECT COUNT(*) FROM sync_state"))
        }
    }

    @Test
    fun `4 to 5 keeps accounts without a colour`() {
        helper.createDatabase(4).use { v4 ->
            v4.execSQL(
                """INSERT INTO accounts (id, name, currency, type, initial_balance_minor, is_shared,
                   owner_member_id, archived, sort_order, excluded_from_total, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('a1', 'Cash', 'ARS', 'CASH', 5000, 1, NULL, 0, 0, 0, 1, NULL, 0, 0)""",
            )
        }

        helper.runMigrationsAndValidate(5, emptyList()).use { v5 ->
            assertEquals("Cash|none", v5.single("SELECT name || '|' || IFNULL(color, 'none') FROM accounts"))
        }
    }

    @Test
    fun `5 to 6 keeps imported operations outside any batch`() {
        helper.createDatabase(5).use { v5 ->
            v5.execSQL(
                """INSERT INTO transactions (id, type, account_id, amount_minor, currency, to_account_id, to_amount_minor,
                   to_currency, category_id, date, note, payee, labels, original_amount_minor, original_currency,
                   author_member_id, import_source, import_external_id, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('t1', 'EXPENSE', 'a1', 100, 'ARS', NULL, NULL, NULL, 'c1', 1, '', NULL, '[]', NULL, NULL,
                   NULL, 'wallet', 'w1', 1, NULL, 0, 0)""",
            )
        }

        helper.runMigrationsAndValidate(6, emptyList()).use { v6 ->
            assertEquals("w1|none", v6.single("SELECT import_external_id || '|' || IFNULL(import_batch_id, 'none') FROM transactions"))
        }
    }

    @Test
    fun `6 to 7 keeps data and adds templates`() {
        helper.createDatabase(6).use { v6 ->
            v6.execSQL(
                """INSERT INTO accounts (id, name, currency, type, initial_balance_minor, is_shared, owner_member_id,
                   archived, sort_order, excluded_from_total, color, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('a1', 'Galicia', 'ARS', 'CARD', 5000, 1, NULL, 0, 0, 0, NULL, 1, NULL, 0, 0)""",
            )
        }

        helper.runMigrationsAndValidate(7, emptyList()).use { v7 ->
            assertEquals("Galicia|5000", v7.single("SELECT name || '|' || initial_balance_minor FROM accounts"))
            v7.execSQL(
                """INSERT INTO templates (id, name, type, account_id, currency, amount_minor, to_account_id, category_id,
                   note, sort_order, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('t1', 'Кофе', 'EXPENSE', 'a1', 'ARS', NULL, NULL, 'c1', '', 0, 1, NULL, 0, 1)""",
            )
            assertEquals("Кофе|ask", v7.single("SELECT name || '|' || IFNULL(amount_minor, 'ask') FROM templates"))
        }
    }

    private fun SQLiteConnection.single(sql: String): String =
        prepare(sql).use { statement ->
            check(statement.step()) { "No rows: $sql" }
            statement.getText(0)
        }
}
