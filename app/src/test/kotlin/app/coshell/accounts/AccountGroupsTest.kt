package app.coshell.accounts

import app.coshell.core.model.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountGroupsTest {
    @Test
    fun groupsInFixedOrderKeepingOrderInside() {
        val items = listOf("cash1" to AccountType.CASH, "card1" to AccountType.CARD, "cash2" to AccountType.CASH)
        val groups = groupByAccountType(items) { it.second }
        assertEquals(listOf(AccountType.CARD, AccountType.CASH), groups.map { it.first })
        assertEquals(listOf("cash1", "cash2"), groups[1].second.map { it.first })
    }
}
