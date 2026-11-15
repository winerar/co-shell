package app.coshell.core.designsystem.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Owner's rule (2026-11-15, #367): the app is built only from the approved set of components.
 * Approved = drawn on the «Компоненты» board (`scripts/agent/mockup_catalog.py`, `docs/design/`).
 * A new component: mock-up → owner's «ок» → board → code (`docs/agents/components.md` §«Утверждённый набор»).
 *
 * [pending] — components that existed before the rule and are not on the board yet (#367: draw every state in
 * both themes, approve, add to the board). The list may only shrink: never add a name here.
 */
class ApprovedComponentsTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    private val sources = File(root, "core/designsystem/src/main/kotlin/app/coshell/core/designsystem")
    private val board = File(root, "scripts/agent/mockup_catalog.py").readText()

    /** Public `@Composable fun Name(` of the design system, without the theme itself and the catalog screens. */
    private val components: Set<String> = sources.walkTopDown()
        .filter { it.extension == "kt" && "catalog" !in it.path.split(File.separatorChar) }
        .flatMap { file -> COMPOSABLE.findAll(file.readText()).map { it.groupValues[1] } }
        .filterNot { it == "CoShellTheme" }
        .toSet()

    private fun onBoard(name: String) = Regex("\\b$name\\b").containsMatchIn(board)

    @Test
    fun everyComponentIsOnTheBoardOrWaitsForApproval() {
        val unapproved = components.filterNot { onBoard(it) || it in pending }.sorted()
        assertTrue(
            "Not on the «Компоненты» board: $unapproved — mock-up → owner's «ок» → board (mockup_catalog.py) → code (#367)",
            unapproved.isEmpty(),
        )
    }

    @Test
    fun approvedComponentsLeaveThePendingList() {
        val done = pending.filter { onBoard(it) || it !in components }.sorted()
        assertTrue("On the board or removed — delete from `pending`: $done", done.isEmpty())
    }

    @Test
    fun theScanFindsComponents() = assertTrue("scan found ${components.size}", components.size > 30 && "CoShellButton" in components)

    private companion object {
        val COMPOSABLE = Regex("""@Composable\s+(?:@\w+(?:\([^)]*\))?\s+)*fun\s+([A-Z]\w*)\s*\(""")

        /** Before the rule, not on the board yet (#367). Only shrinks. */
        val pending = setOf(
        "AmountText",
        "CoShellActionBar",
        "CoShellBarRow",
        "CoShellBullet",
        "CoShellChangePill",
        "CoShellCheckMark",
        "CoShellCheckTile",
        "CoShellChoicePill",
        "CoShellColumns",
        "CoShellConfirmCard",
        "CoShellDayGrid",
        "CoShellDetailTile",
        "CoShellDonut",
        "CoShellExpandIcon",
        "CoShellFab",
        "CoShellFormSection",
        "CoShellHint",
        "CoShellLabelGrid",
        "CoShellMenuRow",
        "CoShellMiniBars",
        "CoShellModalSheet",
        "CoShellModalSheetLayout",
        "CoShellPeekBubble",
        "CoShellRadioMark",
        "CoShellSection",
        "CoShellSectionLink",
        "CoShellSegmented",
        "CoShellSheetLayout",
        "CoShellSwitchLine",
        "CoShellTagChip",
        "CoShellTagDot",
        "CoShellTagMore",
        "CoShellTagMoreChip",
        "CoShellTagPill",
        "MenuIcon",
        "PeekCard",
        "ThemeRevealHost",
        )
    }
}
