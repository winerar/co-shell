package app.coshell.home

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Everything «Обзор» can open: «Операции», the rates screen, an operation, a new account. */
data class HomeActions(
    val onTransactions: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onConverter: () -> Unit = {},
    /** «Без категории» → «Операции» with the «Без категории · Этот месяц» filter. */
    val onUncategorized: () -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
    val onAddAccount: () -> Unit = {},
    /** «Все счета ›» on the balance card → «Счета». */
    val onAccounts: () -> Unit = {},
    /** Sync banner: «Войти» → «Семья», «Отзыв» → feedback with the error code typed in. */
    val onFamily: () -> Unit = {},
    val onFeedback: (prefill: String) -> Unit = {},
    /** The budget tile → «Бюджеты»; an envelope of «Горит в бюджете» → its screen in the current budget month. */
    val onBudgets: () -> Unit = {},
    val onBudget: (categoryId: String, month: YearMonth) -> Unit = { _, _ -> },
    /** «Итог октября» → its screen (#236). */
    val onReview: (month: YearMonth) -> Unit = {},
    /** «Провести» in «Скоро» → the entry sheet filled from the template on the payment's day (#143). */
    val onPostPayment: (templateId: String, date: LocalDate) -> Unit = { _, _ -> },
    /** «Провести» in the credit card line of «Скоро» (#686). */
    val onPostCredit: (accountId: String) -> Unit = {},
    val onOpenDebt: (debtId: String) -> Unit = {},
    val onOpenAccount: (accountId: String) -> Unit = {},
    /** «Все N ›» in «Скоро» → «Регулярные платежи» (#403). */
    val onRecurring: () -> Unit = {},
    /** The «Быстрые заметки» row under the balance → «Черновики» while any wait (#586, #641). */
    val onDrafts: () -> Unit = {},
    /** «Записать» on that row → «Быстрые заметки» (#641). */
    val onNotes: () -> Unit = {},
    /** «Смотреть» on «Что нового · N историй» → the release stories (#693). */
    val onReleaseStories: () -> Unit = {},
)
