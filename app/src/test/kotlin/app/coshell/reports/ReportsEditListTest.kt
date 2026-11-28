package app.coshell.reports

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ReportCard
import app.coshell.core.domain.settings.ReportLayout
import app.coshell.reports.edit.ReportsEditList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The editing list of «Отчёты» (#826): ✕ hides a card into «Скрыты», «+» brings it back at the end of the shown ones. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class ReportsEditListTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var layout by mutableStateOf(ReportLayout())

    private fun show() = compose.setContent {
        CoShellTheme { ReportsEditList(layout, PaddingValues(), onChange = { layout = it }, onDone = {}) }
    }

    @Test
    fun `hide puts the first card under Skryty and plus brings it back`() {
        show()
        compose.onAllNodesWithContentDescription("Скрыть")[0].performClick()
        compose.waitForIdle()
        assertEquals(listOf(ReportCard.SPENDING), layout.hiddenCards)
        compose.onNodeWithText("Скрыты").assertExists()
        compose.onAllNodesWithContentDescription("Показать")[0].performScrollTo().performClick()
        compose.waitForIdle()
        assertEquals(layout.toString(), ReportCard.SPENDING, layout.visible.last())
        assertEquals(emptyList<ReportCard>(), layout.hiddenCards)
    }
}
