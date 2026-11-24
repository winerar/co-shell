package app.coshell.core.designsystem.rules

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every `ic_*.xml` is a thin Material Symbols Outlined 300 icon (`code-style.md` §«Иконки»): an old filled
 * Material Icons file stands out next to the others (#719, the «Экспорт» arrow). Drawn-by-hand icons from an
 * approved board are listed in [custom] with their reason.
 */
class IconSetRuleTest {
    private val custom = setOf(
        "ic_language.xml", // globe from board Fb352A (#352)
        "ic_spark.xml", // «сделал ИИ» spark (#594)
    )

    @Test
    fun `icons come from Material Symbols 300`() {
        val dir = listOf(File("src/main/res/drawable"), File("core/designsystem/src/main/res/drawable")).first { it.isDirectory }
        val offenders = dir.listFiles().orEmpty()
            .filter { it.name.startsWith("ic_") && it.extension == "xml" && it.name !in custom }
            .filter { file -> file.readText().let { "Material Symbols" !in it && it.contains("pathData") } }
            .filter { it.name.startsWith("ic_launcher").not() }
            .map { it.name }
            .sorted()
        assertTrue(
            "Convert with scripts/material-symbol-to-vector.py (code-style.md §«Иконки»):\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }
}
