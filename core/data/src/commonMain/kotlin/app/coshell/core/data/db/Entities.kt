package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room schema. Conventions:
// - money = `<name>_minor INTEGER` + currency TEXT column (see docs/agents/money-and-currency.md);
// - time = epoch milliseconds UTC;
// - sync fields on every synced table: updated_at, updated_by, deleted (tombstone), pending_sync;
// - no foreign keys: with sync, records can arrive in any order and are soft-deleted.

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val currency: String,
    val type: String,
    @ColumnInfo(name = "initial_balance_minor") val initialBalanceMinor: Long,
    @ColumnInfo(name = "is_shared") val isShared: Boolean,
    @ColumnInfo(name = "owner_member_id") val ownerMemberId: String?,
    val archived: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "excluded_from_total", defaultValue = "0") val excludedFromTotal: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val icon: String?,
    val color: Long?,
    @ColumnInfo(name = "parent_id") val parentId: String?,
    val archived: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("account_id"),
        Index("to_account_id"),
        Index("category_id"),
        Index("date"),
        Index(value = ["import_source", "import_external_id"], unique = true),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    val currency: String,
    @ColumnInfo(name = "to_account_id") val toAccountId: String?,
    @ColumnInfo(name = "to_amount_minor") val toAmountMinor: Long?,
    @ColumnInfo(name = "to_currency") val toCurrency: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    val date: Long,
    val note: String,
    val payee: String?,
    /** JSON array of strings. */
    val labels: String,
    @ColumnInfo(name = "original_amount_minor") val originalAmountMinor: Long?,
    @ColumnInfo(name = "original_currency") val originalCurrency: String?,
    @ColumnInfo(name = "author_member_id") val authorMemberId: String?,
    @ColumnInfo(name = "import_source") val importSource: String?,
    @ColumnInfo(name = "import_external_id") val importExternalId: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

@Entity(tableName = "budgets", indices = [Index("category_id", "month")])
data class BudgetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    /** `YYYY-MM`. */
    val month: String,
    @ColumnInfo(name = "limit_minor") val limitMinor: Long,
    val currency: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

/** Sum of transaction effects on one account (one row per account and direction). */
data class BalanceDelta(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "delta_minor") val deltaMinor: Long,
)
