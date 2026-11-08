package app.coshell.core.domain.repository

import app.coshell.core.model.AccountId
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionId
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in `core:data` on top of Room. Soft-deleted transactions are never emitted.
 * Lists are ordered newest first (by date, then by update time).
 */
interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    /** Transactions touching [accountId] either as source or as transfer target. */
    fun observeTransactions(accountId: AccountId): Flow<List<Transaction>>
    suspend fun getTransaction(id: TransactionId): Transaction?
    /** Lookup used by import to skip records that were imported before. */
    suspend fun findImported(source: String, externalId: String): Transaction?
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: TransactionId)
}
