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

@Serializable
data object ScreensSettingsDestination

@Serializable
data object HomeSettingsDestination

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
data class FeedbackDestination(val prefill: String? = null)

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
) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_TRANSACTION_ID = "transactionId"
        const val ARG_TYPE = "type"
        const val ARG_COPY_FROM = "copyFrom"
        const val ARG_TEMPLATE_ID = "templateId"
        const val ARG_PAYMENT_DATE = "paymentDate"
        const val ARG_RECEIPT_QR = "receiptQr"
    }
}

/** «Регулярные платежи» (#143, #403): every template with «Повтор» by its next payment. */
@Serializable
data object RecurringListDestination

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
