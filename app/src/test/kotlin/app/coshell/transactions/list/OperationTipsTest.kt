package app.coshell.transactions.list

import app.coshell.core.domain.hints.HintId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationTipsTest {
    private fun tips(rows: Int, repeated: Boolean = false, filtered: Boolean = false) = operationTips(rows, repeated, filtered).keys.toList()

    @Test
    fun `the tips come one by one as entries pile up`() {
        assertEquals(emptyList<HintId>(), tips(0))
        assertEquals(listOf(HintId.EDIT_TAP), tips(1))
        assertEquals(listOf(HintId.EDIT_TAP, HintId.HOLD_SELECT, HintId.SHARE_RECEIPT), tips(3))
    }

    @Test
    fun `sharing a receipt is told from the third entry and is not told while a filter is on`() {
        assertFalse(HintId.SHARE_RECEIPT in tips(2))
        assertTrue(HintId.SHARE_RECEIPT in tips(3))
        assertFalse(HintId.SHARE_RECEIPT in tips(30, filtered = true))
    }

    @Test
    fun `the share tip comes before the sum and shortcut tips`() {
        val all = tips(SHORTCUTS_ROWS, repeated = true)
        assertTrue(all.indexOf(HintId.SHARE_RECEIPT) < all.indexOf(HintId.HOLD_AMOUNT))
        assertTrue(all.indexOf(HintId.HOLD_AMOUNT) < all.indexOf(HintId.APP_SHORTCUTS))
    }
}
