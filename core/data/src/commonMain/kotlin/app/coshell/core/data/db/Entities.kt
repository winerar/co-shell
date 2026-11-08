package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room rows (KMP Room: no Android-specific types).
// No foreign keys on purpose: sync may deliver a transaction before its account,
// and soft deletion keeps referenced rows anyway.
// Money is stored as minor units + currency code; dates as ISO `yyyy-MM-dd` text
// (sortable, readable); update time as epoch milliseconds.

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
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val icon: String,
    @ColumnInfo(name = "color_argb") val colorArgb: Long,
    @ColumnInfo(name = "parent_id") val parentId: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean,
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("account_id"),
        Index("to_account_id"),
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
    val date: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "to_account_id") val toAccountId: String?,
    @ColumnInfo(name = "to_amount_minor") val toAmountMinor: Long?,
    @ColumnInfo(name = "to_currency") val toCurrency: String?,
    @ColumnInfo(name = "original_amount_minor") val originalAmountMinor: Long?,
    @ColumnInfo(name = "original_currency") val originalCurrency: String?,
    val note: String,
    val payee: String?,
    /** Labels joined with [LABEL_SEPARATOR]. */
    val labels: String,
    @ColumnInfo(name = "author_member_id") val authorMemberId: String?,
    @ColumnInfo(name = "import_source") val importSource: String?,
    @ColumnInfo(name = "import_external_id") val importExternalId: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean,
)

/** ASCII unit separator: cannot be typed in a label. */
const val LABEL_SEPARATOR = '\u001F'
