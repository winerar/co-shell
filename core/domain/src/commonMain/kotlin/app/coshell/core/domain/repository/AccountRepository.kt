package app.coshell.core.domain.repository

import app.coshell.core.model.Account
import app.coshell.core.model.AccountId
import kotlinx.coroutines.flow.Flow

/** Implemented in `core:data` on top of Room. Soft-deleted accounts are never emitted. */
interface AccountRepository {
    fun observeAccounts(includeArchived: Boolean = false): Flow<List<Account>>
    suspend fun getAccount(id: AccountId): Account?
    suspend fun upsert(account: Account)
    /** Soft delete: marks the account deleted so the deletion can be synced. */
    suspend fun delete(id: AccountId)
}
