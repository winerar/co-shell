package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.goals.list.GoalsActions
import app.coshell.goals.list.GoalsScreen
import app.coshell.goals.list.GoalsUiState
import app.coshell.goals.list.goalsPreviewSample
import app.coshell.goals.whatif.WhatIfSheet
import app.coshell.reports.SmartSamples
import app.coshell.reports.components.ConversionCard
import app.coshell.reports.components.InflationCard
import app.coshell.reports.components.InflationGrowthCard
import app.coshell.reports.components.UnusualCard
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.transactionsPreviewSample
import app.coshell.transactions.offer.RecurringOffer
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Smart analytics without AI (#785, boards `Smart786…792`): the new cards, the duplicate mark, the regular-payment offer, «Что если». */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h1400dp-xhdpi", application = Application::class)
class SmartScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Composable
    private fun Cards() = Column(Modifier.verticalScroll(rememberScrollState()).padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        UnusualCard(SmartSamples.unusual, "ноябрь", onCategory = {})
        ConversionCard(SmartSamples.conversion)
        InflationCard(SmartSamples.inflation)
        InflationGrowthCard(SmartSamples.inflation)
    }

    @Test
    fun `report cards dark`() = shot("smart_reports_dark", dark = true) { Cards() }

    @Test
    fun `report cards light`() = shot("smart_reports_light") { Cards() }

    private val goals = GoalsUiState(isLoading = false, active = goalsPreviewSample(), whatIf = mapOf(goalsPreviewSample().first().id to SmartSamples.whatIf))

    @Test
    fun `goal what if line dark`() = shot("smart_goals_dark", dark = true) { GoalsScreen(goals, GoalsActions()) }

    @Test
    fun `goal what if sheet light`() = shot("smart_whatif_sheet_light", dialog = true) { WhatIfSheet(SmartSamples.whatIf, onClosed = {}) }

    private val operations: TransactionsUiState = transactionsPreviewSample.let { state ->
        val rows = state.days.first().rows
        val pair = TransactionsUiState.RowDuplicate("dup:a|b", rows[0].id, rows[1].id)
        state.copy(days = listOf(state.days.first().copy(rows = rows.mapIndexed { i, r -> if (i < 2) r.copy(duplicate = pair) else r })) + state.days.drop(1))
    }

    @Test
    fun `operations with a duplicate and an offer dark`() = shot("smart_operations_dark", dark = true) {
        TransactionsScreen(operations, offer = RecurringOffer("recurring:x", "Telecentro", "25${' '}000${' '}ARS", weekly = false, day = 5))
    }

    private fun shot(name: String, dark: Boolean = false, dialog: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        val root = if (dialog) compose.onAllNodes(isRoot())[1] else compose.onRoot()
        root.captureRoboImage("src/test/screenshots/$name.png")
    }
}
