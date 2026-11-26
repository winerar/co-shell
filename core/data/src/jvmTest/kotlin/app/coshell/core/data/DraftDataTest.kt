package app.coshell.core.data

import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomDraftRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Drafts of quick notes (#586): device-only table, order of writing, «Стереть всё». */
class DraftDataTest {
    private val ars = CurrencyCode("ARS")
    private val db = createInMemoryCoShellDatabase()
    private val drafts = RoomDraftRepository(db.draftDao())

    private fun draft(id: String, at: Long, amount: Long? = 100) =
        Draft(id, "t-$id", ars, amount, "a1", "c1", DraftCategorySource.DICTIONARY, 1_000, at)

    @Test
    fun `drafts come back in the order they were written and keep every field`() = runTest {
        drafts.saveDrafts(listOf(draft("b", 2), draft("a", 1, amount = null).copy(accountId = null, categoryId = null, categorySource = null)))
        val all = drafts.observeDrafts().first()
        assertEquals(listOf("a", "b"), all.map { it.id })
        assertNull(all[0].amountMinor)
        assertNull(all[0].categorySource)
        assertEquals(draft("b", 2), all[1])
        assertEquals(all[1], drafts.getDraft("b"))
    }

    @Test
    fun `saving again replaces a draft and deleting removes it for good`() = runTest {
        drafts.saveDrafts(listOf(draft("a", 1)))
        drafts.saveDrafts(listOf(draft("a", 1).copy(amountMinor = 555)))
        assertEquals(555L, drafts.getDraft("a")?.amountMinor)
        drafts.deleteDrafts(listOf("a", "missing"))
        drafts.deleteDrafts(emptyList())
        assertEquals(emptyList(), drafts.observeDrafts().first())
    }

    @Test
    fun `wipe all empties the drafts`() = runTest {
        drafts.saveDrafts(listOf(draft("a", 1), draft("b", 2)))
        RoomDataWipeRepository(db.wipeDao(), WriteStamper()).wipeAll()
        assertEquals(emptyList(), drafts.observeDrafts().first())
    }
}
