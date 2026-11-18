package app.coshell.transactions.edit.components

import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanPostTest {
    @Test
    fun `done is quiet until the typed amount is not empty`() {
        assertFalse(TransactionEditUiState().canPost)
        assertTrue(TransactionEditUiState(amount = "10").canPost)
        assertFalse(TransactionEditUiState(amount = "10", keypadTarget = KeypadTarget.TO_AMOUNT).canPost)
        assertTrue(TransactionEditUiState(keypadTarget = KeypadTarget.RECEIPT).canPost)
    }
}
