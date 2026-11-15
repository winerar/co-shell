package app.coshell.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.CoShellDatabaseConstructor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Schema v13 and later (`MigrationTest` holds 1→12; split for size). */
class MigrationV13Test {
    private val dir: Path = Files.createTempDirectory("coshell-migration13")
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
    fun `12 to 13 keeps templates without a repeat`() {
        helper.createDatabase(12).use { v12 ->
            v12.execSQL(
                """INSERT INTO templates (id, name, type, account_id, currency, amount_minor, to_account_id, category_id,
                   note, sort_order, updated_at, updated_by, deleted, pending_sync)
                   VALUES ('t1', 'Аренда', 'EXPENSE', 'a1', 'USD', 65000, NULL, NULL, '', 0, 1, NULL, 0, 0)""",
            )
        }

        helper.runMigrationsAndValidate(13, emptyList()).use { v13 ->
            assertEquals("Аренда|none", v13.single("SELECT name || '|' || IFNULL(schedule, 'none') FROM templates"))
            v13.execSQL("UPDATE templates SET schedule = 'M5;2026-11-01;REMIND;' WHERE id = 't1'")
            assertEquals("M5;2026-11-01;REMIND;", v13.single("SELECT schedule FROM templates"))
        }
    }

    private fun SQLiteConnection.single(sql: String): String =
        prepare(sql).use { statement ->
            check(statement.step()) { "No rows: $sql" }
            statement.getText(0)
        }
}
