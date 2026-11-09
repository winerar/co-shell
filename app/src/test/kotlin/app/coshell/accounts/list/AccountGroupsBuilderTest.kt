package app.coshell.accounts.list

import app.coshell.R
import app.coshell.core.model.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountGroupsBuilderTest {
    private fun row(id: String, type: AccountType, currency: String, personalOf: String? = null) =
        AccountsUiState.Row(id, id, type, "", false, personalOf = personalOf, currency = currency)

    private val rows = listOf(
        row("cash", AccountType.CASH, "ARS"),
        row("galicia", AccountType.CARD, "ARS", personalOf = ""),
        row("ameria", AccountType.CARD, "AMD", personalOf = "Аня"),
        row("binance", AccountType.CRYPTO, "USDT"),
    )

    @Test
    fun byTypeInFixedOrder() {
        val groups = buildAccountGroups(rows, AccountGrouping.TYPE, emptySet()) { null }
        assertEquals(listOf("TYPE:CARD", "TYPE:CRYPTO", "TYPE:CASH"), groups.map { it.key })
        assertEquals(listOf("galicia", "ameria"), groups[0].rows.map { it.id })
    }

    @Test
    fun byCurrencyBiggestFirst() {
        val groups = buildAccountGroups(rows, AccountGrouping.CURRENCY, emptySet()) { null }
        assertEquals(listOf("ARS", "AMD", "USDT"), groups.map { it.titleText })
    }

    @Test
    fun byOwnerSharedMineThenOthers() {
        val groups = buildAccountGroups(rows, AccountGrouping.OWNER, emptySet()) { null }
        assertEquals(listOf(R.string.accounts_group_shared, R.string.accounts_group_mine, null), groups.map { it.titleRes })
        assertEquals("Аня", groups[2].titleText)
    }

    @Test
    fun collapsedKeysFoldAndTotalsAreAsked() {
        val groups = buildAccountGroups(rows, AccountGrouping.TYPE, setOf("TYPE:CASH")) { "${it.size}" }
        assertEquals(false, groups.single { it.key == "TYPE:CASH" }.expanded)
        assertEquals("2", groups.first().total)
    }
}
