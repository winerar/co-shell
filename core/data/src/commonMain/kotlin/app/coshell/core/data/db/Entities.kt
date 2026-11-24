package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Room schema. Conventions:
// - money = `<name>_minor INTEGER` + currency TEXT column (see docs/agents/money-and-currency.md);
// - time = epoch milliseconds UTC;
// - sync fields on every synced table: updated_at, updated_by, deleted (tombstone), pending_sync;
// - no foreign keys: with sync, records can arrive in any order and are soft-deleted.

@Serializable
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
    val color: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
    @ColumnInfo(name = "card_digits") val cardDigits: String? = null,
    @ColumnInfo(defaultValue = "0") val prepaid: Boolean = false,
    @ColumnInfo(name = "credit_limit_minor") val creditLimitMinor: Long? = null,
    @ColumnInfo(name = "credit_pay_day") val creditPayDay: Int? = null,
    /** Minimum payment as a share of the debt in basis points (500 = 5 %); `null` — see [creditMinMinor]. */
    @ColumnInfo(name = "credit_min_bp") val creditMinBp: Int? = null,
    @ColumnInfo(name = "credit_min_minor") val creditMinMinor: Long? = null,
    /** Statement day and grace days (#634, v22): a reminder only, both or neither. */
    @ColumnInfo(name = "credit_statement_day") val creditStatementDay: Int? = null,
    @ColumnInfo(name = "credit_grace_days") val creditGraceDays: Int? = null,
    /** A deposit's conditions (#634, v23): yearly rate in basis points, the closing day `YYYY-MM-DD` and the payout name; all three or none. */
    @ColumnInfo(name = "deposit_rate_bp") val depositRateBp: Int? = null,
    @ColumnInfo(name = "deposit_end") val depositEnd: String? = null,
    @ColumnInfo(name = "deposit_payout") val depositPayout: String? = null,
    /** The day the deposit was opened (#634, v24): optional, `YYYY-MM-DD`; lets «в конце срока» count the whole term. */
    @ColumnInfo(name = "deposit_start") val depositStart: String? = null,
    /** «Только у меня» (#558, v25): the row and its operations never leave the phone; not in the sync JSON. */
    @Transient @ColumnInfo(name = "local_only", defaultValue = "0") val localOnly: Boolean = false,
)

@Serializable
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
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

@Serializable
@Entity(
    tableName = "transactions",
    indices = [
        Index("account_id"),
        Index("to_account_id"),
        Index("category_id"),
        Index("date"),
        Index(value = ["import_source", "import_external_id"], unique = true),
        Index("import_batch_id"),
        Index("receipt_key"),
        // Sync reads `WHERE pending_sync = 1` on every poll and the banner count after every write (#259).
        Index("pending_sync"),
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
    @ColumnInfo(name = "import_batch_id") val importBatchId: String? = null,
    /** Scanned receipt (#526, v15): `seller:point:type:number`; not unique — a deliberate second record of one receipt is allowed. */
    @ColumnInfo(name = "receipt_key") val receiptKey: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

/**
 * Limit of a category or a folder. [month] (`YYYY-MM`) is the first month it applies to — the name is
 * kept from v1, where a limit was for one month (synced fields are never renamed). Schema v8 added
 * [note], [rollover] and [lastMonth] (`YYYY-MM`, inclusive; `NULL` — no end). Schema v16 (#576): a zero limit
 * is stored as [limitMinor] = 1 + [zeroLimit] — older versions require a positive limit and would crash on 0,
 * so they read "one cent" (overspent at once, as intended).
 */
@Serializable
@Entity(tableName = "budgets", indices = [Index("category_id", "month")])
data class BudgetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val month: String,
    @ColumnInfo(name = "limit_minor") val limitMinor: Long,
    val currency: String,
    val note: String? = null,
    @ColumnInfo(defaultValue = "0") val rollover: Boolean = false,
    @ColumnInfo(name = "last_month") val lastMonth: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
    @ColumnInfo(name = "zero_limit", defaultValue = "0") val zeroLimit: Boolean = false,
)

/**
 * Operation template (schema v7; v13 — `schedule`; v14 — `account_currency`). [amountMinor] `NULL` — the amount
 * is asked every time. [currency] is the currency of the amount; [accountCurrency] is set only when the account's
 * differs (#415, rent 650 USD from an ARS account). An older version then reads "650 USD on an account in another
 * currency" — the account no longer fits, so it neither posts by itself nor counts 650 in ARS.
 */
@Serializable
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    val currency: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long?,
    @ColumnInfo(name = "to_account_id") val toAccountId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    val note: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    /** «Повтор» (v13, #143) as `ScheduleCodec` text; `NULL` — an ordinary template. Default for older sync records. */
    val schedule: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
    /** Currency of the account when it differs from [currency] (v14, #415); `NULL` — the same. Default for older sync records. */
    @ColumnInfo(name = "account_currency") val accountCurrency: String? = null,
)

/**
 * One family setting (schema v9, #122): [id] is the key (`report_currencies`, `month_start_day`),
 * [value] its text form — parsed by `RoomFamilySettings`. Synced like the other rows (`kind` = `setting`).
 */
@Serializable
@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val id: String,
    val value: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

/**
 * Operation tag (schema v11, #129): name + colour. Operations reference it from `transactions.labels`
 * (`TagIds.of`), so the table has no link column. Synced (`kind` = `tag`).
 */
@Serializable
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Long? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

/** Sum of transaction effects on one account (one row per account and direction). */
data class BalanceDelta(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "delta_minor") val deltaMinor: Long,
)

/**
 * One event of a debt (#552, `DebtEvent`): issued, returned in part, closed. Synced (`kind` = `debtEvent`), in the backup, wiped by «Стереть всё».
 * Events are only added or soft-deleted, so two phones never overwrite one another. `date` — epoch ms UTC, `due_date` — `YYYY-MM-DD`.
 */
@Serializable
@Entity(tableName = "debt_events")
data class DebtEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    val kind: String,
    val direction: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    val currency: String,
    @ColumnInfo(name = "account_id") val accountId: String? = null,
    val date: Long,
    val person: String = "",
    @ColumnInfo(name = "due_date") val dueDate: String? = null,
    val note: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "updated_by") val updatedBy: String?,
    val deleted: Boolean = false,
    @Transient @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true,
)

