package app.coshell.reports.review

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.reports.review.MonthReviewUiState.Change
import app.coshell.reports.review.MonthReviewUiState.Limits
import kotlinx.datetime.YearMonth

/** Numbers of boards `Mon236Screen`, `Mon236Limits`, `Mon236Edge` (October 2026, USD): previews and screenshot tests. */
object MonthReviewSamples {
    private const val NBSP = ' '

    private fun limit(id: String, name: String, icon: String, color: Long, spent: Long, limit: Long, currency: String = "ARS"): Envelope {
        val over = spent > limit
        fun n(value: Long) = "%,d".format(java.util.Locale.ROOT, value).replace(',', NBSP)
        val caption = when {
            over -> Caption.Over("${n(spent - limit)}$NBSP$currency")
            spent == 0L -> Caption.Idle
            spent == limit -> Caption.Paid
            else -> Caption.Left("${n(limit - spent)}$NBSP$currency", perDay = null)
        }
        val tone = when {
            over -> BulletTone.DANGER
            spent == 0L -> BulletTone.IDLE
            else -> BulletTone.NORMAL
        }
        return Envelope(id, name, icon, color, null, n(spent), "${n(limit)}$NBSP$currency", spent.toFloat() / limit, null, tone, caption)
    }

    private val many = listOf(
        limit("cafe", "Кафе", "cafe", 0xFFFF7043, 95_000, 80_000),
        limit("gifts", "Подарки", "gift", 0xFFEC407A, 120, 50, "USD"),
        limit("subs", "Подписки", "subscriptions", 0xFF7E57C2, 27_000, 25_000),
        limit("home", "Дом", "home", 0xFF26C6DA, 372_000, 400_000),
        limit("groceries", "Продукты", "food", 0xFF66BB6A, 338_000, 350_000),
        limit("study", "Образование", "education", 0xFF5C6BC0, 160, 160, "USD"),
        limit("transport", "Транспорт", "transport", 0xFF42A5F5, 52_000, 60_000),
        limit("sport", "Спорт", "sport", 0xFFFFA726, 45_000, 45_000),
        limit("pharmacy", "Аптека", "pharmacy", 0xFFEF5350, 31_000, 40_000),
        limit("taxi", "Такси", "taxi", 0xFFFFCA28, 18_000, 30_000),
        limit("beauty", "Красота", "beauty", 0xFFAB47BC, 22_000, 40_000),
        limit("clothes", "Одежда", "clothes", 0xFF8D6E63, 61_000, 100_000),
        limit("phone", "Связь", "phone", 0xFF29B6F6, 19_000, 25_000),
        limit("books", "Книги", "books", 0xFF9CCC65, 20, 40, "USD"),
        limit("doctors", "Врачи", "health", 0xFFEF5350, 0, 50_000),
        limit("pets", "Питомцы", "pets", 0xFFFFB74D, 12_000, 30_000),
        limit("delivery", "Доставка", "restaurant", 0xFF4DB6AC, 9_000, 20_000),
        limit("cinema", "Кино", "movies", 0xFFBA68C8, 6_000, 15_000),
    )

    val state = MonthReviewUiState(
        month = YearMonth(2026, 10),
        currency = "USD",
        saved = "750",
        savingsRate = 36,
        spent = "1${NBSP}350",
        earned = "2${NBSP}100",
        usualMonths = 3,
        changes = listOf(
            Change("gifts", "Подарки", "gift", 0xFFEC407A, "120", "30", "+90", 300, more = true),
            Change("home", "Дом", "home", 0xFF26C6DA, "290", "365", "-75", -21, more = false),
            Change("cafe", "Кафе", "cafe", 0xFFFF7043, "180", "110", "+70", 64, more = true),
        ),
        limits = Limits(within = 4, over = many.take(2), all = many.take(2) + many.drop(3).take(4)),
    )

    /** «Все лимиты октября» on 18 limits. */
    val manyLimits = state.copy(limits = Limits(within = 15, over = many.take(3), all = many))

    /** No income, no history, no limits, a currency without a rate (`Mon236Edge`). */
    val edge = state.copy(
        saved = "-1${NBSP}350",
        overspent = true,
        savingsRate = null,
        earned = "0",
        usualMonths = 0,
        changes = emptyList(),
        limits = null,
        missing = listOf("VES"),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MonthReviewPreview() = CoShellTheme(darkTheme = true) { MonthReviewScreen(MonthReviewSamples.state) }

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MonthLimitsPreview() = CoShellTheme(darkTheme = true) { MonthLimitsScreen(MonthReviewSamples.manyLimits, onBack = {}, onLimit = {}) }
