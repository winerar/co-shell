package app.coshell.core.data

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

val RUB = CurrencyCode("RUB")
val EUR = CurrencyCode("EUR")
val meta = SyncMeta(updatedAt = Instant.fromEpochMilliseconds(1_000), updatedBy = MemberId("m1"))

val card = Account(
    AccountId("card"), "Карта", RUB, AccountType.CARD, initialBalanceMinor = -500,
    isShared = false, ownerMemberId = MemberId("m1"), meta = meta,
)
val cash = Account(AccountId("cash"), "Наличные €", EUR, AccountType.CASH, meta = meta)
val food = Category(CategoryId("food"), "Еда", CategoryType.EXPENSE, "cart", 0xFF4CAF50, meta = meta)
val cafe = food.copy(id = CategoryId("cafe"), name = "Кафе", parentId = food.id)

val coffee = Transaction(
    id = TransactionId("t1"), type = TransactionType.EXPENSE, accountId = card.id,
    amount = Money(35_000, RUB), date = LocalDate(2026, 11, 8), categoryId = cafe.id,
    originalAmount = Money(400, EUR), note = "капучино", payee = "Кофейня",
    labels = listOf("отпуск", "рим"), authorMemberId = MemberId("m1"),
    importSource = "wallet", importExternalId = "w-42", meta = meta,
)
val exchange = Transaction(
    id = TransactionId("t2"), type = TransactionType.TRANSFER, accountId = card.id,
    amount = Money(1_000_000, RUB), toAccountId = cash.id, toAmount = Money(10_000, EUR),
    date = LocalDate(2026, 11, 7), meta = meta,
)
