package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Read-only checks that the stored data hangs together (#219): every query returns the ids of live rows
 * that break one rule, at most [IntegrityDao.SAMPLE] of them. Nothing is repaired here.
 */
@Dao
interface IntegrityDao {
    @Query(
        """SELECT t.id FROM transactions t LEFT JOIN accounts a ON a.id = t.account_id
           WHERE t.deleted = 0 AND (a.id IS NULL OR a.deleted = 1) LIMIT $SAMPLE""",
    )
    suspend fun onMissingAccount(): List<String>

    @Query(
        """SELECT t.id FROM transactions t LEFT JOIN accounts a ON a.id = t.to_account_id
           WHERE t.deleted = 0 AND t.type = 'TRANSFER' AND (a.id IS NULL OR a.deleted = 1) LIMIT $SAMPLE""",
    )
    suspend fun transferToMissingAccount(): List<String>

    @Query(
        """SELECT t.id FROM transactions t LEFT JOIN categories c ON c.id = t.category_id
           WHERE t.deleted = 0 AND t.category_id IS NOT NULL AND (c.id IS NULL OR c.deleted = 1) LIMIT $SAMPLE""",
    )
    suspend fun onMissingCategory(): List<String>

    @Query(
        """SELECT t.id FROM transactions t JOIN accounts a ON a.id = t.account_id
           WHERE t.deleted = 0 AND t.currency != a.currency LIMIT $SAMPLE""",
    )
    suspend fun currencyNotOfAccount(): List<String>

    @Query(
        """SELECT t.id FROM transactions t JOIN accounts a ON a.id = t.to_account_id
           WHERE t.deleted = 0 AND t.type = 'TRANSFER' AND t.to_currency IS NOT NULL AND t.to_currency != a.currency
           LIMIT $SAMPLE""",
    )
    suspend fun transferCurrencyNotOfAccount(): List<String>

    @Query(
        """SELECT b.id FROM budgets b LEFT JOIN categories c ON c.id = b.category_id
           WHERE b.deleted = 0 AND (c.id IS NULL OR c.deleted = 1) LIMIT $SAMPLE""",
    )
    suspend fun budgetOnMissingCategory(): List<String>

    @Query(
        """SELECT c.id FROM categories c LEFT JOIN categories p ON p.id = c.parent_id
           WHERE c.deleted = 0 AND c.parent_id IS NOT NULL AND (p.id IS NULL OR p.deleted = 1) LIMIT $SAMPLE""",
    )
    suspend fun categoryInMissingFolder(): List<String>

    companion object {
        const val SAMPLE = 20
    }
}
