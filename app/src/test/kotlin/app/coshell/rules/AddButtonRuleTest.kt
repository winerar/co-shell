package app.coshell.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Owner's rule (2026-11-16, #408): every list screen has ONE add button — the round «+» `CoShellAddFab`, bottom right,
 * and this is the style of every future add button. No «+» in the header, no add row in the list, no dashed «add» pill.
 */
class AddButtonRuleTest {
    private val sources = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        .resolve("app/src/main/kotlin/app/coshell")

    private fun text(path: String) = sources.resolve(path).readText()

    @Test
    fun everyListScreenHasTheRoundAddButton() {
        val missing = LIST_SCREENS.filter { "CoShellAddFab(" !in text(it) }
        assertTrue("List screens without CoShellAddFab: $missing (#408)", missing.isEmpty())
    }

    @Test
    fun listScreensHaveNoOtherWayToAdd() {
        val other = LIST_SCREENS.filter { "ic_add" in text(it) || "CoShellExtendedFab" in text(it) || "dashedBorder" in text(it) }
        assertTrue("Another add button on $other: use only CoShellAddFab (#408)", other.isEmpty())
    }

    @Test
    fun theScreensExist() = assertTrue(LIST_SCREENS.all { sources.resolve(it).exists() })

    private companion object {
        val LIST_SCREENS = listOf(
            "accounts/list/AccountsScreen.kt", "categories/list/CategoriesScreen.kt", "templates/list/TemplatesScreen.kt",
            "recurring/list/RecurringListScreen.kt", "budgets/list/BudgetsScreen.kt", "rates/list/RatesScreen.kt",
            "currencies/list/ReportCurrenciesScreen.kt",
        )
    }
}
