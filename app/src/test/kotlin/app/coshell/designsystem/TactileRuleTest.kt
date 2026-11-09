package app.coshell.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Keeps the motion language whole (design-system.md §«Движение»): every interactive element on a
 * screen goes through `core:designsystem` (CoShellButton, CoShellFilterChip, tactileClickable…),
 * so it sinks, tilts, catches light and springs back like all the others. A raw Material button or
 * a plain `clickable` would react differently — this test names the file to fix. The same for the base
 * blocks (fields, dialogs, list rows, cards): screens take them from `core:designsystem` too.
 */
class TactileRuleTest {
    private val forbidden = listOf(
        Regex("""^import androidx\.compose\.material3\.(Button|TextButton|OutlinedButton|FilledTonalButton|ElevatedButton|IconButton|FilledIconButton|FloatingActionButton|ExtendedFloatingActionButton|FilterChip|InputChip|AssistChip|SuggestionChip|SegmentedButton|Switch|Checkbox|RadioButton|NavigationBarItem)$""", RegexOption.MULTILINE),
        Regex("""^import androidx\.compose\.foundation\.(clickable|combinedClickable|selection\.toggleable|selection\.selectable)$""", RegexOption.MULTILINE),
        // Base components: CoShellTextField/AmountField, CoShellDialog/AlertDialog, CoShellListRow/SwitchRow, CoShellCard/Message,
        // CoShellSheet/SheetLayout, CoShellBottomBar, CoShellLinearProgress.
        Regex("""^import androidx\.compose\.material3\.(OutlinedTextField|TextField|AlertDialog|BasicAlertDialog|ListItem|Card|OutlinedCard|ElevatedCard|ModalBottomSheet|NavigationBar|LinearProgressIndicator)$""", RegexOption.MULTILINE),
    )

    @Test
    fun `screens use only tactile components`() {
        val root = listOf(File("src/main/kotlin"), File("app/src/main/kotlin")).first { it.isDirectory }
        val offenders = root.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.flatMap { rule -> rule.findAll(text).map { "${file.relativeTo(root)}: ${it.value}" }.toList() }
            }
            .toList()
        assertTrue("Use core:designsystem components instead:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
