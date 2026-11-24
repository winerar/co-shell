package app.coshell.screenshots

import app.coshell.home.HomeScreen
import app.coshell.home.homeBudgetPreviewSample
import app.coshell.home.homePreviewSample
import app.coshell.reports.review.MonthLimitsScreen
import app.coshell.reports.review.MonthReviewSamples
import app.coshell.reports.recap.MonthRecapSamples
import app.coshell.reports.recap.MonthRecapScreen
import app.coshell.reports.review.MonthReviewScreen

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.reports.ReportsActions
import app.coshell.reports.ReportsSamples
import app.coshell.reports.capital.CapitalMonthActions
import app.coshell.reports.capital.CapitalMonthSamples
import app.coshell.reports.capital.CapitalMonthScreen
import app.coshell.reports.ReportsScreen
import app.coshell.reports.all.ReportCategoriesScreen
import app.coshell.reports.category.ReportCategorySamples
import app.coshell.reports.category.ReportCategoryScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * «Отчёты» (#140) on [ReportsSamples] — the numbers of board `RepA`: the tab, empty period, «Все категории» (`RepAll`),
 * «Из чего сложилось» (`RepDrill`, #188); «Итог месяца» (#236, `Mon236*`) and its card on «Обзор».
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ReportsScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `reports dark`() = shot("reports_dark", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test
    fun `reports light`() = shot("reports_light") { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test @Config(qualifiers = "+en")
    fun `reports english`() = shot("reports_en", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test @Config(qualifiers = "+es")
    fun `reports spanish`() = shot("reports_es", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test
    fun `reports empty period`() = shot("reports_empty", dark = true) { ReportsScreen(ReportsSamples.empty, ReportsActions()) }

    @Test
    fun `all categories`() = shot("report_categories_dark", dark = true) { ReportCategoriesScreen(ReportsSamples.state, onBack = {}) }

    @Test
    fun `category drill dark`() = shot("report_category_dark", dark = true) { drill() }

    @Test
    fun `category drill light`() = shot("report_category_light") { drill() }

    // «Итоги октября» (#554, boards Rec554*): each story, the first in both themes.
    @Test
    fun `month recap dark`() = shot("month_recap_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state) }

    @Test
    fun `month recap light`() = shot("month_recap_light") { MonthRecapScreen(MonthRecapSamples.state) }

    @Test
    fun `month recap comparison`() = shot("month_recap_vs_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 1) }

    @Test
    fun `month recap weeks`() = shot("month_recap_weeks_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 2) }

    @Test
    fun `month recap new category`() = shot("month_recap_new_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 3) }

    @Test
    fun `month recap weekend`() = shot("month_recap_weekend_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 4) }

    @Test
    fun `month recap streak`() = shot("month_recap_streak_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 5) }

    @Test
    fun `month recap final`() = shot("month_recap_final_dark", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 6) }

    @Test @Config(qualifiers = "+en")
    fun `month recap english`() = shot("month_recap_en", dark = true) { MonthRecapScreen(MonthRecapSamples.state, startAt = 6) }

    @Test
    fun `month review dark`() = shot("month_review_dark", dark = true) { MonthReviewScreen(MonthReviewSamples.state) }

    @Test
    fun `month review light`() = shot("month_review_light") { MonthReviewScreen(MonthReviewSamples.state) }

    @Test
    fun `month review edge cases`() = shot("month_review_edge_dark", dark = true) { MonthReviewScreen(MonthReviewSamples.edge) }

    @Test
    fun `month review limits`() = shot("month_review_limits_dark", dark = true) {
        MonthLimitsScreen(MonthReviewSamples.manyLimits, onBack = {}, onLimit = {})
    }

    @Test @Config(qualifiers = "+en")
    fun `month review english`() = shot("month_review_en", dark = true) { MonthReviewScreen(MonthReviewSamples.state) }

    @Test
    fun `home with the month review`() = shot("home_review_dark", dark = true) {
        HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample, review = MonthReviewSamples.state)
    }

    /** Board `Tr238DrillA`: «↑ 45% больше обычного · к 11-му обычно 61» and the card «По месяцам». */
    @Test
    fun `category drill trend`() = shot("report_category_trend_dark", dark = true) {
        ReportCategoryScreen(ReportCategorySamples.trend, onBack = {}, onTransactionClick = {})
    }

    @Test
    fun `category drill trend light`() = shot("report_category_trend_light") {
        ReportCategoryScreen(ReportCategorySamples.trend, onBack = {}, onTransactionClick = {})
    }

    /** Board `Rp189NwDrill` (#373): October picked on the line, 24 accounts in groups. */
    @Test
    fun `capital month dark`() = shot("capital_month_dark", dark = true) { CapitalMonthScreen(CapitalMonthSamples.state, CapitalMonthActions()) }

    @Test
    fun `capital month light`() = shot("capital_month_light") { CapitalMonthScreen(CapitalMonthSamples.state, CapitalMonthActions()) }

    @Test
    fun `capital month by change`() = shot("capital_month_change_dark", dark = true) { CapitalMonthScreen(CapitalMonthSamples.byChange, CapitalMonthActions()) }

    @Composable
    private fun drill() = ReportCategoryScreen(ReportCategorySamples.state, onBack = {}, onTransactionClick = {})

    // The other theme of references that had one (#382: every screen in both themes).
    @Test
    fun `all categories light`() = shot("report_categories_light") { ReportCategoriesScreen(ReportsSamples.state, onBack = {}) }

    @Test
    fun `month review limits light`() = shot("month_review_limits_light") {
        MonthLimitsScreen(MonthReviewSamples.manyLimits, onBack = {}, onLimit = {})
    }

    @Test
    fun `home with the month review light`() = shot("home_review_light") {
        HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample, review = MonthReviewSamples.state)
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
    }
}
