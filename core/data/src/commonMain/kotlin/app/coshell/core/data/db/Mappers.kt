package app.coshell.core.data.db

import app.coshell.core.model.Account
import app.coshell.core.model.AccountId
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryId
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MemberId
import app.coshell.core.model.Money
import app.coshell.core.model.SyncMeta
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionId
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

private fun meta(updatedAt: Long, updatedBy: String?, deleted: Boolean) = SyncMeta(
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    updatedBy = updatedBy?.let(::MemberId),
    deleted = deleted,
)

private fun money(amountMinor: Long?, currency: String?): Money? =
    if (amountMinor != null && currency != null) Money(amountMinor, CurrencyCode(currency)) else null

fun AccountEntity.toModel() = Account(
    id = AccountId(id),
    name = name,
    currency = CurrencyCode(currency),
    type = AccountType.valueOf(type),
    initialBalanceMinor = initialBalanceMinor,
    isShared = isShared,
    ownerMemberId = ownerMemberId?.let(::MemberId),
    archived = archived,
    meta = meta(updatedAt, updatedBy, deleted),
)

fun Account.toEntity() = AccountEntity(
    id = id.value,
    name = name,
    currency = currency.value,
    type = type.name,
    initialBalanceMinor = initialBalanceMinor,
    isShared = isShared,
    ownerMemberId = ownerMemberId?.value,
    archived = archived,
    updatedAt = meta.updatedAt.toEpochMilliseconds(),
    updatedBy = meta.updatedBy?.value,
    deleted = meta.deleted,
)

fun CategoryEntity.toModel() = Category(
    id = CategoryId(id),
    name = name,
    type = CategoryType.valueOf(type),
    icon = icon,
    colorArgb = colorArgb,
    parentId = parentId?.let(::CategoryId),
    meta = meta(updatedAt, updatedBy, deleted),
)

fun Category.toEntity() = CategoryEntity(
    id = id.value,
    name = name,
    type = type.name,
    icon = icon,
    colorArgb = colorArgb,
    parentId = parentId?.value,
    updatedAt = meta.updatedAt.toEpochMilliseconds(),
    updatedBy = meta.updatedBy?.value,
    deleted = meta.deleted,
)

fun TransactionEntity.toModel() = Transaction(
    id = TransactionId(id),
    type = TransactionType.valueOf(type),
    accountId = AccountId(accountId),
    amount = Money(amountMinor, CurrencyCode(currency)),
    date = LocalDate.parse(date),
    categoryId = categoryId?.let(::CategoryId),
    toAccountId = toAccountId?.let(::AccountId),
    toAmount = money(toAmountMinor, toCurrency),
    originalAmount = money(originalAmountMinor, originalCurrency),
    note = note,
    payee = payee,
    labels = if (labels.isEmpty()) emptyList() else labels.split(LABEL_SEPARATOR),
    authorMemberId = authorMemberId?.let(::MemberId),
    importSource = importSource,
    importExternalId = importExternalId,
    meta = meta(updatedAt, updatedBy, deleted),
)

fun Transaction.toEntity(): TransactionEntity {
    require(labels.none { LABEL_SEPARATOR in it }) { "Label contains a reserved character" }
    return TransactionEntity(
        id = id.value,
        type = type.name,
        accountId = accountId.value,
        amountMinor = amount.amountMinor,
        currency = amount.currency.value,
        date = date.toString(),
        categoryId = categoryId?.value,
        toAccountId = toAccountId?.value,
        toAmountMinor = toAmount?.amountMinor,
        toCurrency = toAmount?.currency?.value,
        originalAmountMinor = originalAmount?.amountMinor,
        originalCurrency = originalAmount?.currency?.value,
        note = note,
        payee = payee,
        labels = labels.joinToString(LABEL_SEPARATOR.toString()),
        authorMemberId = authorMemberId?.value,
        importSource = importSource,
        importExternalId = importExternalId,
        updatedAt = meta.updatedAt.toEpochMilliseconds(),
        updatedBy = meta.updatedBy?.value,
        deleted = meta.deleted,
    )
}
