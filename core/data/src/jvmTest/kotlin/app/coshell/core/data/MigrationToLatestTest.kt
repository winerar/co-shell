package app.coshell.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.room.useReaderConnection
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.CoShellDatabaseConstructor
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 1.0 check (#383): a phone may skip any number of updates, so every released schema must reach the
 * current one in one go with its rows intact. Each table of version N gets one row built from the
 * exported schema itself, so a new version is covered without editing this test.
 * Step-by-step data checks live in `MigrationTest` / `MigrationV13Test`.
 */
class MigrationToLatestTest {
    private val schemaDir = File("schemas/${CoShellDatabase::class.qualifiedName}")
    private val dir: Path = Files.createTempDirectory("coshell-migration-latest")

    private val versions: List<Int> =
        schemaDir.listFiles().orEmpty().mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }.sorted()
    private val latest get() = versions.last()

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun `schema files run from 1 without gaps up to the database version`() = runTest {
        assertEquals((1..latest).toList(), versions, "every released version must keep its schema JSON")
        val db = createInMemoryCoShellDatabase()
        val current = db.useReaderConnection { connection ->
            connection.usePrepared("PRAGMA user_version") { it.step(); it.getLong(0).toInt() }
        }
        db.close()
        assertEquals(latest, current, "bump of @Database(version) without an exported schema")
    }

    @Test
    fun `every released schema migrates straight to the current one with rows intact`() {
        for (from in versions.dropLast(1)) {
            val helper = helper(from)
            val tables = tablesOf(from)
            helper.createDatabase(from).use { old -> tables.forEach { old.insertSample(it) } }
            helper.runMigrationsAndValidate(latest, emptyList()).use { new ->
                tables.forEach { table ->
                    assertEquals(1L, new.count(table.name), "v$from → v$latest lost the row of ${table.name}")
                    val key = table.primaryKey.joinToString(" || '|' || ")
                    assertEquals(table.sampleKey(), new.text("SELECT $key FROM ${table.name}"), "key of ${table.name}")
                }
            }
        }
    }

    @Test
    fun `the database is never rebuilt destructively`() {
        val sources = File("src").walkTopDown().filter { it.isFile && it.extension == "kt" && "Test" !in it.path }.toList()
        assertTrue(sources.any { it.name == "DatabaseFactory.kt" }, "source scan sees no database factory")
        val offenders = sources.filter { "fallbackToDestructiveMigration" in it.readText() }.toList()
        assertTrue(offenders.isEmpty(), "fallbackToDestructiveMigration erases a family's data: $offenders")
    }

    private fun helper(version: Int) = MigrationTestHelper(
        schemaDirectoryPath = Path.of("schemas"),
        databasePath = dir.resolve("v$version.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = CoShellDatabase::class,
        databaseFactory = { CoShellDatabaseConstructor.initialize() },
    )

    private class Column(val name: String, val affinity: String)
    private class Table(val name: String, val columns: List<Column>, val primaryKey: List<String>) {
        fun sample(column: Column): String = when (column.affinity) {
            "INTEGER" -> "1"
            "REAL" -> "1.5"
            else -> "'${column.name}-1'"
        }
        fun sampleKey(): String = primaryKey.joinToString("|") { key ->
            val column = columns.first { it.name == key }
            if (column.affinity == "TEXT") "$key-1" else sample(column)
        }
    }

    private fun tablesOf(version: Int): List<Table> {
        val root = Json.parseToJsonElement(File(schemaDir, "$version.json").readText()).jsonObject
        return root.obj("database")["entities"]!!.jsonArray.map { entity ->
            val e = entity.jsonObject
            Table(
                name = e.str("tableName"),
                columns = e["fields"]!!.jsonArray.map { Column(it.jsonObject.str("columnName"), it.jsonObject.str("affinity")) },
                primaryKey = e.obj("primaryKey")["columnNames"]!!.jsonArray.map { it.jsonPrimitive.content }
                    .also { check(!e.obj("primaryKey")["autoGenerate"]!!.jsonPrimitive.boolean) },
            )
        }
    }

    private fun SQLiteConnection.insertSample(table: Table) {
        val names = table.columns.joinToString { it.name }
        val values = table.columns.joinToString { table.sample(it) }
        prepare("INSERT INTO ${table.name} ($names) VALUES ($values)").use { it.step() }
    }

    private fun SQLiteConnection.count(table: String): Long =
        prepare("SELECT COUNT(*) FROM $table").use { it.step(); it.getLong(0) }

    private fun SQLiteConnection.text(sql: String): String =
        prepare(sql).use { check(it.step()) { "No rows: $sql" }; it.getText(0) }

    private fun JsonObject.obj(key: String) = this[key]!!.jsonObject
    private fun JsonObject.str(key: String) = this[key]!!.jsonPrimitive.content
}
