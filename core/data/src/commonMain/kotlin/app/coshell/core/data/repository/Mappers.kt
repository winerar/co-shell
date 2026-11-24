package app.coshell.core.data.repository

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.data.db.BudgetEntity
import app.coshell.core.data.db.CategoryEntity
import app.coshell.core.data.db.DebtEventEntity
import app.coshell.core.data.db.TagEntity
import app.coshell.core.data.db.TemplateEntity
import app.coshell.core.data.db.TransactionEntity
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.MinPayment
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import app.coshell.core.model.DebtEvent
import app.coshell.core.model.DebtEventKind
import app.coshell.core.model.Money
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
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
    cardDigits = cardDigits,
    prepaid = prepaid,
    credit = credit(),
    deposit = depositTermsOf(depositRateBp, depositEnd, depositPayout, depositStart),
    localOnly = localOnly,
)

/** The credit conditions of a stored account; anything unreadable (a half-written row of another version) is an ordinary account. */
private fun AccountEntity.credit(): CreditTerms? = creditTermsOf(creditLimitMinor, creditPayDay, creditMinBp, creditMinMinor, creditStatementDay, creditGraceDays)

internal fun creditTermsOf(limitMinor: Long?, payDay: Int?, minBp: Int?, minMinor: Long?, statementDay: Int? = null, graceDays: Int? = null): CreditTerms? {
    val limit = limitMinor ?: return null
    val day = payDay ?: return null
    val min = minBp?.let { MinPayment.Percent(it) } ?: minMinor?.let { MinPayment.Fixed(it) } ?: return null
    // A half-written grace period (one of the two) is dropped, the card stays a card.
    val grace = if (statementDay != null && graceDays != null) statementDay to graceDays else null
    return runCatching { CreditTerms(limit, day, min, grace?.first, grace?.second) }.getOrNull()
}

/** The conditions of a stored deposit; anything unreadable (a payout of a newer version, half a row) is a deposit without conditions. */
internal fun depositTermsOf(rateBp: Int?, end: String?, payout: String?, start: String? = null): DepositTerms? {
    val rate = rateBp ?: return null
    val day = end?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val how = payout?.let { name -> DepositPayout.entries.firstOrNull { it.name == name } } ?: return null
    // An unreadable opening day only loses the whole-term income, the conditions stay.
    val opened = start?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    return runCatching { DepositTerms(rate, day, how, opened) }.getOrNull() ?: runCatching { DepositTerms(rate, day, how) }.getOrNull()
}

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
    cardDigits = cardDigits,
    prepaid = prepaid,
    creditLimitMinor = credit?.limitMinor,
    creditPayDay = credit?.payDay,
    creditMinBp = (credit?.minPayment as? MinPayment.Percent)?.basisPoints,
    creditMinMinor = (credit?.minPayment as? MinPayment.Fixed)?.minor,
    creditStatementDay = credit?.statementDay,
    creditGraceDays = credit?.graceDays,
    depositRateBp = deposit?.rateBp,
    depositEnd = deposit?.endDate?.toString(),
    depositPayout = deposit?.payout?.name,
    depositStart = deposit?.startDate?.toString(),
    updatedAt = stamp.now,
    updatedBy = stamp.by,
    localOnly = localOnly,
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
    labels = decodeLabels(labels),
    originalAmount = moneyOrNull(originalAmountMinor, originalCurrency),
    authorMemberId = authorMemberId,
    importSource = importSource,
    importExternalId = importExternalId,
    importBatchId = importBatchId,
    receiptKey = receiptKey,
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
    importBatchId = importBatchId,
    receiptKey = receiptKey,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

internal fun BudgetEntity.toModel() = Budget(
    id = id,
    categoryId = categoryId,
    startMonth = YearMonth.parse(month),
    limit = Money(if (zeroLimit) 0 else limitMinor, CurrencyCode(currency)),
    note = note?.takeIf { it.isNotBlank() },
    rollover = rollover,
    lastMonth = lastMonth?.let { YearMonth.parse(it) },
)

internal fun Budget.toEntity(stamp: Stamp) = BudgetEntity(
    id = id,
    categoryId = categoryId,
    month = startMonth.toString(),
    limitMinor = storedLimitMinor(limit),
    currency = limit.currency.value,
    note = note,
    rollover = rollover,
    lastMonth = lastMonth?.toString(),
    updatedAt = stamp.now,
    updatedBy = stamp.by,
    zeroLimit = limit.isZero,
)

/** A zero limit goes out as one minimal unit: older versions require a positive limit (see `BudgetEntity`). */
internal fun storedLimitMinor(limit: Money): Long = if (limit.isZero) 1 else limit.amountMinor

internal fun TemplateEntity.toModel() = TransactionTemplate(
    id = id,
    name = name,
    type = TransactionType.valueOf(type),
    accountId = accountId,
    currency = CurrencyCode(accountCurrency ?: currency),
    amount = moneyOrNull(amountMinor, currency),
    categoryId = categoryId,
    toAccountId = toAccountId,
    note = note,
    sortOrder = sortOrder,
    schedule = ScheduleCodec.decode(schedule),
)

internal fun TransactionTemplate.toEntity(stamp: Stamp) = TemplateEntity(
    id = id,
    name = name,
    type = type.name,
    accountId = accountId,
    currency = (amount?.currency ?: currency).value,
    accountCurrency = currency.value.takeIf { isForeign },
    amountMinor = amount?.amountMinor,
    toAccountId = toAccountId,
    categoryId = categoryId,
    note = note,
    sortOrder = sortOrder,
    schedule = schedule?.let(ScheduleCodec::encode),
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

private fun moneyOrNull(minor: Long?, currency: String?): Money? =
    if (minor != null && currency != null) Money(minor, CurrencyCode(currency)) else null

internal fun TagEntity.toModel() = Tag(id = id, name = name, color = color)

internal fun Tag.toEntity(stamp: Stamp) = TagEntity(id = id, name = name, color = color, updatedAt = stamp.now, updatedBy = stamp.by)

/** `transactions.labels` (a JSON array of strings) as a list. */
internal fun decodeLabels(json: String): List<String> = Json.decodeFromString(labelsSerializer, json)

internal fun DebtEventEntity.toModel() = DebtEvent(
    id = id,
    debtId = debtId,
    kind = DebtEventKind.valueOf(kind),
    direction = DebtDirection.valueOf(direction),
    amount = Money(amountMinor, CurrencyCode(currency)),
    date = Instant.fromEpochMilliseconds(date),
    accountId = accountId,
    person = person,
    dueDate = dueDate?.let(LocalDate::parse),
    note = note,
)

internal fun DebtEvent.toEntity(stamp: Stamp) = DebtEventEntity(
    id = id,
    debtId = debtId,
    kind = kind.name,
    direction = direction.name,
    amountMinor = amount.amountMinor,
    currency = amount.currency.value,
    accountId = accountId,
    date = date.toEpochMilliseconds(),
    person = person,
    dueDate = dueDate?.toString(),
    note = note,
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

