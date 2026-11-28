package app.coshell.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes.

@Serializable
data object HomeDestination

@Serializable
data object TransactionsDestination

@Serializable
data object AccountsDestination

/** «Отчёты» tab (#140). */
@Serializable
data object ReportsDestination

/** «Все категории» of the report open in the tab under it (shares its ViewModel). */
@Serializable
data object ReportCategoriesDestination

/**
 * «Из чего сложилось» (#188): one category of a report — the period ([kind], ISO days [start] ..< [end], [firstYear]-[firstMonth],
 * the family's month [startDay]), the report [currency] and whether it is compared with the period before.
 */
@Serializable
data class ReportCategoryDestination(
    val categoryId: String,
    val kind: String,
    val start: String,
    val end: String,
    val firstYear: Int,
    val firstMonth: Int,
    val startDay: Int,
    val currency: String,
    val compare: Boolean,
)

/**
 * «Капитал · октябрь» (#373, board `Rp189NwDrill`): the accounts at the end of [year]-[month] (a family month from
 * [startDay]) in the report [currency]; the line shows the year up to [lastYear]-[lastMonth], as on «Отчёты».
 */
@Serializable
data class CapitalMonthDestination(
    val year: Int,
    val month: Int,
    val lastYear: Int,
    val lastMonth: Int,
    val startDay: Int,
    val currency: String,
)

/** «Итоги октября» (#554): stories of [year]-[month]; the card on «Обзор» opens them, the last story — [MonthReviewDestination]. */
@Serializable
data class MonthRecapDestination(val year: Int, val month: Int)

/** «Что нового» as stories (#693): opened from the card on «Обзор» once after an update. */
@Serializable
data object ReleaseStoriesDestination

/** «Знакомство с приложением» (#598): from the card on a new «Обзор» and «Ещё → Настройки → Помощь». */
@Serializable
data object TourDestination

/** «Жесты» (#797): the cheat sheet of every gesture, «Ещё → Настройки → Помощь → Жесты». */
@Serializable
data object GesturesDestination

/** «Итог месяца» (#236) of [year]-[month] (the family's month). */
@Serializable
data class MonthReviewDestination(val year: Int, val month: Int)

/** «Лимиты октября» of the review under it (shares its ViewModel). */
@Serializable
data object MonthReviewLimitsDestination

/** Account editor; `accountId == null` creates a new account. */
@Serializable
data class AccountEditDestination(val accountId: String? = null) {
    companion object {
        /** SavedStateHandle key of [accountId]; must match the property name. */
        const val ARG_ACCOUNT_ID = "accountId"
    }
}

@Serializable
data object MoreDestination

@Serializable
data object SettingsDestination

@Serializable
data object AppearanceDestination

/** «Ещё → Конфиденциальность» (#145). */
@Serializable
data object PrivacyDestination

/** PIN screen of the settings: `mode` — [app.coshell.privacy.pin.PinSetupMode] name. */
@Serializable
data class PinSetupDestination(val mode: String)

@Serializable
data object ScreensSettingsDestination

@Serializable
data object HomeSettingsDestination

/** «Экраны → Нижняя панель» (#154). */
@Serializable
data object BarSettingsDestination

@Serializable
data object HomeDetailsSettingsDestination

@Serializable
data object BalanceAccountsDestination

@Serializable
data object OpsSettingsDestination

@Serializable
data object ReportsSettingsDestination

@Serializable
data object AccountsSettingsDestination

@Serializable
data object MoneySettingsDestination

@Serializable
data object InputSettingsDestination

@Serializable
data object NoticeSettingsDestination

@Serializable
data object DataSettingsDestination

/** «Настройки → Помощь»: hints, the tour, gestures, invite code, privacy policy (poll 2026-11-28 «А»). */
@Serializable
data object HelpSettingsDestination

@Serializable
data object CategoriesDestination

@Serializable
data object BackupDestination

@Serializable
data object WalletImportDestination

@Serializable
data object WipeDataDestination

@Serializable
data object ExportDestination

@Serializable
data object FamilyDestination

@Serializable
data object UpdateDestination

/** «Прошлые версии» from the update screen (#284). */
@Serializable
data object PastReleasesDestination

/** [prefill] — text typed in for the user (the sync banner's error code). */
@Serializable
data class FeedbackDestination(
    val prefill: String? = null,
    /** From «Не удалось прочитать чек» (#561): theme «Формат чека» with the shared file attached. */
    val receiptSample: Boolean = false,
)

/** One sent message by its send time (#157). */
@Serializable
data class FeedbackDetailDestination(val sentAtMillis: Long)

@Serializable
data object RatesDestination

/** The converter sheet (#319): opened from «Обзор», «Ещё → Деньги» and «Курсы». */
@Serializable
data object ConverterDestination

@Serializable
data object ReportCurrenciesDestination

/**
 * Category editor. `categoryId == null` creates a new category of [type] (a [CategoryType] name),
 * optionally inside folder [parentId]; [asFolder] opens it as a new folder.
 */
@Serializable
data class CategoryEditDestination(
    val categoryId: String? = null,
    val type: String = "EXPENSE",
    val parentId: String? = null,
    val asFolder: Boolean = false,
) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_CATEGORY_ID = "categoryId"
        const val ARG_TYPE = "type"
        const val ARG_PARENT_ID = "parentId"
        const val ARG_AS_FOLDER = "asFolder"
    }
}

/**
 * Transaction editor. `transactionId == null` creates a new transaction of [type]
 * (a [app.coshell.core.model.TransactionType] name).
 */
/** Scanner of a receipt QR (#526): the camera; a receipt QR opens the entry sheet filled from it, «Ввести вручную» an empty one. */
@Serializable
data object ReceiptScanDestination

@Serializable
data class TransactionEditDestination(
    val transactionId: String? = null,
    val type: String = "EXPENSE",
    /** A new operation filled from this one (swipe right in «Операции» — «Дублировать»), dated today. */
    val copyFrom: String? = null,
    /** A new operation filled from this template (tap in «Все шаблоны»), dated today. */
    val templateId: String? = null,
    /**
     * «Провести» a regular payment (#143): with [templateId], the operation is dated this day (ISO `2026-11-15`) and,
     * once saved, every payment of the template up to it counts as handled.
     */
    val paymentDate: String? = null,
    /** Text with the QR link of a receipt (#526): the sheet opens with its amount and date, a new expense. */
    val receiptQr: String? = null,
    /** A shared receipt nothing could read (#561): the blank sheet opens with «Не удалось прочитать чек». */
    val receiptUnread: Boolean = false,
    /** A draft of a quick note (#586): the sheet opens filled from it; once saved, the draft is gone. */
    val draftId: String? = null,
    /** «Провести» a credit card payment (#686): a new transfer into this card for the minimum payment; the source account is chosen. */
    val creditCardId: String? = null,
    /** «Пополнить» a savings goal (#768): a new transfer into this account for [topUpMinor] (0 — no amount); the source account is chosen. */
    val topUpAccountId: String? = null,
    val topUpMinor: Long = 0,
) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_CREDIT_CARD = "creditCardId"
        const val ARG_TOP_UP_ACCOUNT = "topUpAccountId"
        const val ARG_TOP_UP_MINOR = "topUpMinor"
        const val ARG_TRANSACTION_ID = "transactionId"
        const val ARG_TYPE = "type"
        const val ARG_COPY_FROM = "copyFrom"
        const val ARG_TEMPLATE_ID = "templateId"
        const val ARG_PAYMENT_DATE = "paymentDate"
        const val ARG_RECEIPT_QR = "receiptQr"
        const val ARG_RECEIPT_UNREAD = "receiptUnread"
        const val ARG_DRAFT_ID = "draftId"
    }
}

/** «Регулярные платежи» (#143, #403): every template with «Повтор» by its next payment; [year] — opens on «За год» (#771). */
@Serializable
data class RecurringListDestination(val year: Boolean = false) {
    companion object {
        /** SavedStateHandle key of [year]; must match the property name. */
        const val ARG_YEAR = "year"
    }
}

/** «Быстрые заметки» (#586): the lines of expenses; «Обработать» makes drafts of them. */
@Serializable
data object NotesDestination

/** «Черновики» (#586): drafts from the notes, checked and posted here. */
@Serializable
data object DraftsDestination

/** «Все шаблоны»: search, «Часто», groups by type and category (#104). */
@Serializable
data object TemplatesDestination

/** Template editor; `templateId == null` creates a new template. */
@Serializable
data class TemplateEditDestination(val templateId: String? = null) {
    companion object {
        /** SavedStateHandle key of [templateId]; must match the property name. */
        const val ARG_TEMPLATE_ID = "templateId"
    }
}

@Serializable
data object BudgetsDestination

/** One envelope (#183): the limit on [categoryId] in [year]-[month], its categories and operations. */
@Serializable
data class BudgetDetailDestination(val categoryId: String, val year: Int, val month: Int) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_CATEGORY_ID = "categoryId"
        const val ARG_YEAR = "year"
        const val ARG_MONTH = "month"
    }
}

/**
 * Limit editor (#103). `categoryId == null` — a new limit; the change applies from [year]-[month] on,
 * earlier months keep their limit.
 */
@Serializable
data class BudgetEditDestination(val categoryId: String? = null, val year: Int, val month: Int) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_CATEGORY_ID = "categoryId"
        const val ARG_YEAR = "year"
        const val ARG_MONTH = "month"
    }
}

/** «Цели» (#768): savings goals over account balances. */
@Serializable
data object GoalsDestination

/** The goal sheet (#768): a new goal without [goalId], else that goal with «Пополнить». */
@Serializable
data class GoalEditDestination(val goalId: String? = null) {
    companion object {
        /** SavedStateHandle key of [goalId]; must match the property name. */
        const val ARG_GOAL_ID = "goalId"
    }
}

/** «Долги» (#552): «Мне должны» and «Я должен». */
@Serializable
data object DebtsDestination

/** The new-debt sheet (#552). */
@Serializable
data object DebtNewDestination

/** One debt's card (#552): returns, «Вернули часть», «Закрыть долг». */
@Serializable
data class DebtDetailDestination(val debtId: String) {
    companion object {
        /** SavedStateHandle key of [debtId]; must match the property name. */
        const val ARG_DEBT_ID = "debtId"
    }
}
