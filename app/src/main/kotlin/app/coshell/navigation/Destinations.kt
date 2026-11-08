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
