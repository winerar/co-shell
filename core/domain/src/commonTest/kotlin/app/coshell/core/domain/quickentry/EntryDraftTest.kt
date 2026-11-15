package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class EntryDraftTest {
    private val ars = CurrencyCode("ARS")
    private val savedAt = Instant.parse("2026-11-13T12:00:00Z")
    private val draft = EntryDraft(TransactionType.EXPENSE, "galicia", Money.ofMajor(3_500, currency = ars), "cafe", "с Аней", emptyList(), savedAt)

    @Test
    fun `amount or note is worth keeping, nothing typed is not`() {
        assertTrue(EntryDrafts.worthKeeping(Money.ofMajor(1, currency = ars), ""))
        assertTrue(EntryDrafts.worthKeeping(null, "кофе"))
        assertFalse(EntryDrafts.worthKeeping(Money.zero(ars), "  "))
        assertFalse(EntryDrafts.worthKeeping(null, ""))
    }

    @Test
    fun `fresh draft is offered`() {
        assertEquals(draft, EntryDrafts.toOffer(draft, savedAt + 5.minutes))
        assertEquals(draft, EntryDrafts.toOffer(draft, savedAt + 12.hours))
    }

    @Test
    fun `old draft, no draft and clock moved back are not offered`() {
        assertNull(EntryDrafts.toOffer(draft, savedAt + 12.hours + 1.minutes))
        assertNull(EntryDrafts.toOffer(null, savedAt))
        assertNull(EntryDrafts.toOffer(draft, savedAt - 1.minutes))
    }
}
