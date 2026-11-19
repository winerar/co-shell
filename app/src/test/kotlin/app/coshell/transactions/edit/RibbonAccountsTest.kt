package app.coshell.transactions.edit

import app.coshell.core.model.AccountType
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import org.junit.Test
import org.junit.Assert.assertEquals

class RibbonAccountsTest {
    private fun option(id: String) = AccountOption(id = id, name = id, currency = "ARS", type = AccountType.CARD)
    private val all = ('a'..'h').map { option(it.toString()) }

    @Test
    fun `recent accounts come first then the others up to five`() {
        val ids = ribbonAccounts(all, listOf("e", "c"), selectedId = "e").map { it.id }
        assertEquals(listOf("e", "c", "a", "b", "d"), ids)
    }

    @Test
    fun `the chosen account is always shown and takes the last place`() {
        val ids = ribbonAccounts(all, listOf("a", "b", "c", "d", "e"), selectedId = "h").map { it.id }
        assertEquals(listOf("a", "b", "c", "d", "h"), ids)
    }

    @Test
    fun `the source of a transfer is left out`() {
        val ids = ribbonAccounts(all.take(3), emptyList(), selectedId = null, excludeId = "a").map { it.id }
        assertEquals(listOf("b", "c"), ids)
    }
}
