package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.AccountId
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryId
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionId
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

val RUB = CurrencyCode("RUB")
val EUR = CurrencyCode("EUR")
val date = LocalDate(2026, 11, 8)

val rubCard = Account(AccountId("card"), "Карта", RUB, AccountType.CARD, 100_000, meta = testMeta)
val eurCash = Account(AccountId("cash"), "Наличные €", EUR, AccountType.CASH, 0, meta = testMeta)
val food = Category(CategoryId("food"), "Еда", CategoryType.EXPENSE, meta = testMeta)
val salary = Category(CategoryId("salary"), "Зарплата", CategoryType.INCOME, meta = testMeta)

fun tx(
    id: String,
    type: TransactionType,
    amount: Money,
    account: Account = rubCard,
    category: Category? = null,
    to: Account? = null,
    toAmount: Money? = null,
) = Transaction(
    id = TransactionId(id), type = type, accountId = account.id, amount = amount, date = date,
    categoryId = category?.id, toAccountId = to?.id, toAmount = toAmount, meta = testMeta,
)
