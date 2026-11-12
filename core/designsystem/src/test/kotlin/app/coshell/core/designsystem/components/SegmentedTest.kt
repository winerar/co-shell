package app.coshell.core.designsystem.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SegmentedTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val picked = mutableListOf<Int>()

    @Before
    fun setUp() {
        compose.setContent {
            CoShellTheme {
                var selected by remember { mutableIntStateOf(0) }
                CoShellSegmented(LABELS, selected, { selected = it; picked += it }, tone = SegmentTone.View)
            }
        }
    }

    @Test
    fun `only the selected segment is selected`() {
        compose.onNodeWithText("По типу").assertIsSelected()
        compose.onNodeWithText("По валюте").assertIsNotSelected()
        compose.onNodeWithText("Чьи").assertIsNotSelected()
    }

    @Test
    fun `tap selects the segment and reports its index`() {
        compose.onNodeWithText("Чьи").performClick()

        assertEquals(listOf(2), picked)
        compose.onNodeWithText("Чьи").assertIsSelected()
        compose.onNodeWithText("По типу").assertIsNotSelected()
    }

    private companion object {
        val LABELS = listOf("По типу", "По валюте", "Чьи")
    }
}
