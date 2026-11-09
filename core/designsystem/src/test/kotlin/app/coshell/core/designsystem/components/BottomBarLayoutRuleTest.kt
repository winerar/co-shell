package app.coshell.core.designsystem.components

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * The Scaffold measures its bottom bar with the whole screen as the max height. Anything inside
 * the bar that fills the max height (`fillMaxHeight`, `fillMaxSize`) stretches the bar over the
 * screen and leaves every screen 0 px tall — the bar sits in the middle and tabs seem dead.
 */
class BottomBarLayoutRuleTest {
    @Test
    fun `bottom bar never fills the max height`() {
        val path = "kotlin/app/coshell/core/designsystem/components/BottomBar.kt"
        val file = listOf(File("src/main/$path"), File("core/designsystem/src/main/$path")).first { it.isFile }
        val text = file.readText().lines().filterNot { it.trimStart().startsWith("//") }.joinToString("\n")
        assertFalse(Regex("""fillMax(Height|Size)\(""").containsMatchIn(text))
    }
}
