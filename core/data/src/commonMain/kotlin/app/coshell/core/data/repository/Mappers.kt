package app.coshell.core.data.repository

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.data.db.BudgetEntity
import app.coshell.core.data.db.CategoryEntity
import app.coshell.core.data.db.TransactionEntity
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.YearMonth
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/** Who and when changed a record; written into the sync columns. */
internal data class Stamp(val now: Long, val by: String?)

private val labelsSerializer = ListSerializer(String.serializer())

internal fun AccountEntity.toModel() = Account(
    id = id,
    name = name,
    currency = CurrencyCode(currency),
    type = AccountType.valueOf(type),
    initialBalanceMinor = initialBalanceMinor,
    isShared = isShared,
    ownerMemberId = ownerMemberId,
    archived = archived,
    sortOrder = sortOrder,
    excludedFromTotal = excludedFromTotal,
    color = color,
)

internal fun Account.toEntity(stamp: Stamp) = AccountEntity(
    id = id,
    name = name,
    currency = currency.value,
    type = type.name,
    initialBalanceMinor = initialBalanceMinor,
    isShared = isShared,
    ownerMemberId = ownerMemberId,
    archived = archived,
    sortOrder = sortOrder,
    excludedFromTotal = excludedFromTotal,
    color = color,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

internal fun CategoryEntity.toModel() = Category(
    id = id,
    name = name,
    type = CategoryType.valueOf(type),
    icon = icon,
    color = color,
    parentId = parentId,
    archived = archived,
    sortOrder = sortOrder,
)

internal fun Category.toEntity(stamp: Stamp) = CategoryEntity(
    id = id,
    name = name,
    type = type.name,
    icon = icon,
    color = color,
    parentId = parentId,
    archived = archived,
    sortOrder = sortOrder,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

internal fun TransactionEntity.toModel() = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    accountId = accountId,
    amount = Money(amountMinor, CurrencyCode(currency)),
    date = Instant.fromEpochMilliseconds(date),
    categoryId = categoryId,
    toAccountId = toAccountId,
    toAmount = moneyOrNull(toAmountMinor, toCurrency),
    note = note,
    payee = payee,
    labels = Json.decodeFromString(labelsSerializer, labels),
    originalAmount = moneyOrNull(originalAmountMinor, originalCurrency),
    authorMemberId = authorMemberId,
    importSource = importSource,
    importExternalId = importExternalId,
)

internal fun Transaction.toEntity(stamp: Stamp) = TransactionEntity(
    id = id,
    type = type.name,
    accountId = accountId,
    amountMinor = amount.amountMinor,
    currency = amount.currency.value,
    toAccountId = toAccountId,
    toAmountMinor = toAmount?.amountMinor,
    toCurrency = toAmount?.currency?.value,
    categoryId = categoryId,
    date = date.toEpochMilliseconds(),
    note = note,
    payee = payee,
    labels = Json.encodeToString(labelsSerializer, labels),
    originalAmountMinor = originalAmount?.amountMinor,
    originalCurrency = originalAmount?.currency?.value,
    authorMemberId = authorMemberId,
    importSource = importSource,
    importExternalId = importExternalId,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

internal fun BudgetEntity.toModel() = Budget(
    id = id,
    categoryId = categoryId,
    month = YearMonth.parse(month),
    limit = Money(limitMinor, CurrencyCode(currency)),
)

internal fun Budget.toEntity(stamp: Stamp) = BudgetEntity(
    id = id,
    categoryId = categoryId,
    month = month.toString(),
    limitMinor = limit.amountMinor,
    currency = limit.currency.value,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

private fun moneyOrNull(minor: Long?, currency: String?): Money? =
    if (minor != null && currency != null) Money(minor, CurrencyCode(currency)) else null
