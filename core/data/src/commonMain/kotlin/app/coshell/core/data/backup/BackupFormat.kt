package app.coshell.core.data.backup

import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Backup file format. Independent of the Room schema on purpose: the DB can change, old backups
// must still import. Bump [BackupFile.CURRENT_VERSION] only for incompatible changes; adding an
// optional field with a default does not need a bump.

@Serializable
internal data class BackupFile(
    val format: String = FORMAT,
    val version: Int = CURRENT_VERSION,
    val exportedAt: String,
    val accounts: List<AccountDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val transactions: List<TransactionDto> = emptyList(),
    val budgets: List<BudgetDto> = emptyList(),
    /** Since schema v2; downloaded rates are not backed up (they can be downloaded again). */
    val manualRates: List<ManualRateDto> = emptyList(),
) {
    companion object {
        const val FORMAT = "co-shell-backup"
        const val CURRENT_VERSION = 1
    }
}

@Serializable
internal data class MoneyDto(val minor: Long, val currency: String) {
    fun toModel() = Money(minor, CurrencyCode(currency))
}

private fun Money.toDto() = MoneyDto(amountMinor, currency.value)

@Serializable
internal data class AccountDto(
    val id: String,
    val name: String,
    val currency: String,
    val type: String,
    val initialBalanceMinor: Long = 0,
    val isShared: Boolean = true,
    val ownerMemberId: String? = null,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
) {
    fun toModel() = Account(
        id, name, CurrencyCode(currency), AccountType.valueOf(type), initialBalanceMinor,
        isShared, ownerMemberId, archived, sortOrder,
    )
}

internal fun Account.toDto() = AccountDto(
    id, name, currency.value, type.name, initialBalanceMinor, isShared, ownerMemberId, archived, sortOrder,
)

@Serializable
internal data class CategoryDto(
    val id: String,
    val name: String,
    val type: String,
    val icon: String? = null,
    val color: Long? = null,
    val parentId: String? = null,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
) {
    fun toModel() = Category(id, name, CategoryType.valueOf(type), icon, color, parentId, archived, sortOrder)
}

internal fun Category.toDto() = CategoryDto(id, name, type.name, icon, color, parentId, archived, sortOrder)

@Serializable
internal data class TransactionDto(
    val id: String,
    val type: String,
    val accountId: String,
    val amount: MoneyDto,
    /** ISO-8601 instant, UTC. */
    val date: String,
    val categoryId: String? = null,
    val toAccountId: String? = null,
    val toAmount: MoneyDto? = null,
    val note: String = "",
    val payee: String? = null,
    val labels: List<String> = emptyList(),
    val originalAmount: MoneyDto? = null,
    val authorMemberId: String? = null,
    val importSource: String? = null,
    val importExternalId: String? = null,
) {
    fun toModel() = Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        accountId = accountId,
        amount = amount.toModel(),
        date = Instant.parse(date),
        categoryId = categoryId,
        toAccountId = toAccountId,
        toAmount = toAmount?.toModel(),
        note = note,
        payee = payee,
        labels = labels,
        originalAmount = originalAmount?.toModel(),
        authorMemberId = authorMemberId,
        importSource = importSource,
        importExternalId = importExternalId,
    )
}

internal fun Transaction.toDto() = TransactionDto(
    id, type.name, accountId, amount.toDto(), date.toString(), categoryId, toAccountId, toAmount?.toDto(),
    note, payee, labels, originalAmount?.toDto(), authorMemberId, importSource, importExternalId,
)

@Serializable
internal data class BudgetDto(val id: String, val categoryId: String, val month: String, val limit: MoneyDto) {
    fun toModel() = Budget(id, categoryId, YearMonth.parse(month), limit.toModel())
}

internal fun Budget.toDto() = BudgetDto(id, categoryId, month.toString(), limit.toDto())

/** `1 from = rate to` on [date] (`YYYY-MM-DD`); [rate] is a plain decimal string, never a JSON number. */
@Serializable
internal data class ManualRateDto(val date: String, val from: String, val to: String, val rate: String) {
    fun toModel() = ExchangeRate(LocalDate.parse(date), CurrencyCode(from), CurrencyCode(to), Rate.parse(rate), RateOrigin.MANUAL)
}

internal fun ExchangeRate.toManualDto() = ManualRateDto(date.toString(), from.value, to.value, rate.toPlainString())
