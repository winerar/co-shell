package app.coshell.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cards are only of the theme's two accents, the hero apart (owner 2026-11-20): `CoShellCard`/`CoShellTile` take a
 * `CardTone`, never a colour. `CardTone.Warning`/`Danger` are an exception the owner allowed only for the states below;
 * a new place needs the owner's «ок» first, then a line here.
 */
class CardToneRuleTest {
    private val alertAllowed = setOf(
        "app/coshell/home/components/SyncBanner.kt", // sync keeps failing
        "app/coshell/home/components/VersionGateBanner.kt", // entry closed until the update
        "app/coshell/home/components/HomeTiles.kt", // rates missing or old
        "app/coshell/home/components/BudgetTile.kt", // a limit running out or overspent
        "app/coshell/wipe/components/WipeConfirm.kt", // wiping all the data
    )

    private val alert = Regex("""\bCardTone\.(Warning|Danger)\b""")

    @Test
    fun `warning and danger cards only where the owner allowed them`() {
        val root = listOf(File("src/main/kotlin"), File("app/src/main/kotlin")).first { it.isDirectory }
        val offenders = root.walkTopDown()
            .filter { it.extension == "kt" && !it.name.endsWith("Previews.kt") }
            .filter { alert.containsMatchIn(it.readText()) }
            .map { it.relativeTo(root).invariantSeparatorsPath }
            .filter { it !in alertAllowed }
            .toList()
        assertTrue("CardTone.Warning/Danger outside the allowed states (CardToneRuleTest.alertAllowed):\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }

    @Test
    fun `allowed places still use an alert tone`() {
        val root = listOf(File("src/main/kotlin"), File("app/src/main/kotlin")).first { it.isDirectory }
        val stale = alertAllowed.filterNot { alert.containsMatchIn(File(root, it).readText()) }
        assertTrue("Remove from CardToneRuleTest.alertAllowed:\n" + stale.joinToString("\n"), stale.isEmpty())
    }
}
