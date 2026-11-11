package app.coshell.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes.

@Serializable
data object HomeDestination

@Serializable
data object TransactionsDestination

@Serializable
data object AccountsDestination

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
data object CategoriesDestination

@Serializable
data object BackupDestination

@Serializable
data object WalletImportDestination

@Serializable
data object WipeDataDestination

@Serializable
data object FamilyDestination

@Serializable
data object UpdateDestination

/** [prefill] — text typed in for the user (the sync banner's error code). */
@Serializable
data class FeedbackDestination(val prefill: String? = null)

/** One sent message by its send time (#157). */
@Serializable
data class FeedbackDetailDestination(val sentAtMillis: Long)

@Serializable
data object RatesDestination

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
) {
    companion object {
        // SavedStateHandle keys; must match the property names.
        const val ARG_TRANSACTION_ID = "transactionId"
        const val ARG_TYPE = "type"
        const val ARG_COPY_FROM = "copyFrom"
        const val ARG_TEMPLATE_ID = "templateId"
    }
}

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
