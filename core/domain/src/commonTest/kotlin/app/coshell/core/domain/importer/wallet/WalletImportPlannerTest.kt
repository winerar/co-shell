package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.SAMPLE
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.TimeZone

class WalletImportPlannerTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private var n = 0
    private val planner = WalletImportPlanner { "id${n++}" }
    private val records = WalletCsvParser.parse(SAMPLE).records
    private val adjustment = mapOf(CategoryType.EXPENSE to "adj-", CategoryType.INCOME to "adj+")

    /** A co-shell USD card with 50 USD already on it. */
    private val card = AccountWithBalance(Account("card", "карта usd", usd, initialBalanceMinor = 5_000), Money(5_000, usd))

    private fun choices(walletBalances: Map<String, Money> = emptyMap()) = WalletImportChoices(
        accounts = WalletAccountMatcher.suggest(WalletAccountMatcher.accounts(records), listOf(card.account)),
        categories = WalletCategoryMatcher.suggest(WalletCategoryMatcher.keys(records), emptyList()),
        walletBalances = walletBalances,
        timeZone = TimeZone.UTC,
    )

    private fun plan(choices: WalletImportChoices = choices(), known: Set<String> = emptySet()) =
        planner.plan(records, choices, listOf(card), adjustment, known)

    @Test
    fun newAccountsAndCategoriesAreCreatedAndEveryOperationJoinsTheBatch() {
        val plan = plan()
        assertEquals(emptyList(), plan.issues)
        assertEquals(10, plan.newCount)
        assertEquals(0, plan.alreadyImported)
        // «Карта USD» merged into the existing card (same name ignoring case, same currency).
        assertEquals(setOf("Наличные ARS", "Карта RUB", "Счёт AMD"), plan.write.accounts.map { it.name }.toSet())
        assertEquals(6, plan.write.categories.size)
        assertEquals(3, plan.newAccounts)
        assertTrue(plan.write.transactions.all { it.importBatchId == plan.batchId })
        assertTrue(plan.write.transactions.any { it.accountId == "card" })
    }

    @Test
    fun balancesAfterImportIncludeTheExistingBalance() {
        val plan = plan()
        // 50 − 10 (courses) + 1000 (from AMD) − 500 (to ARS) − 20 (unpaired → adjustment) = 520 USD.
        assertEquals(Money(52_000, usd), plan.balancesAfter.getValue("card"))
        val cash = plan.write.accounts.single { it.currency == ars }
        assertEquals(Money(73_500_000, ars), plan.balancesAfter.getValue(cash.id))
        assertEquals("Наличные ARS", plan.accountNames.getValue(cash.id))
    }

    @Test
    fun knownRecordsAreSkipped() {
        val first = plan()
        val again = plan(known = first.write.transactions.mapNotNull { it.importExternalId }.toSet())
        assertEquals(0, again.newCount)
        assertEquals(10, again.alreadyImported)
        assertEquals(Money(5_000, usd), again.balancesAfter["card"] ?: card.balance)
    }

    @Test
    fun walletBalanceShiftsTheInitialBalance() {
        val plan = plan(choices(mapOf("Карта USD" to Money(60_000, usd), "Наличные ARS" to Money(70_000_000, ars))))
        val card = plan.write.accounts.single { it.id == "card" }
        assertEquals(5_000L + 60_000 - 52_000, card.initialBalanceMinor)
        assertEquals(Money(60_000, usd), plan.balancesAfter.getValue("card"))
        val cash = plan.write.accounts.single { it.currency == ars }
        assertEquals(70_000_000L - 73_500_000, cash.initialBalanceMinor)
    }

    @Test
    fun unpairedTransferMayGoToAnAccountOutsideTheFile() {
        val savings = AccountWithBalance(Account("savings", "Копилка", usd), Money(0, usd))
        val unpairedLine = records.single { it.isTransfer && it.amount == Money(-2_000, usd) }.line
        val plan = planner.plan(
            records, choices().copy(unpaired = mapOf(unpairedLine to UnpairedChoice.TransferWith("savings"))),
            listOf(card, savings), adjustment, emptySet(),
        )
        assertEquals(emptyList(), plan.issues)
        val transfer = plan.write.transactions.single { it.toAccountId == "savings" }
        assertEquals("card", transfer.accountId)
        assertEquals(Money(2_000, usd), plan.balancesAfter.getValue("savings"))
    }

    @Test
    fun twoWalletAccountsCreatedUnderOneNameBecomeOneAccount() {
        val base = choices()
        val merged = base.copy(
            accounts = base.accounts + ("Карта USD" to AccountChoice.CreateNew("Наличные ARS ", ars)),
        )
        val plan = plan(merged)
        assertEquals(1, plan.write.accounts.count { it.name == "Наличные ARS" })
    }
}
