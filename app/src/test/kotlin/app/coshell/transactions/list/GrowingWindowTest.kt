package app.coshell.transactions.list

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowingWindowTest {
    private val rows = MutableStateFlow((1..5).toList())

    @Test
    fun `grows by a page until everything is loaded`() = runTest(UnconfinedTestDispatcher()) {
        val window = GrowingWindow(pageSize = 2)
        val pages = window.observe { limit -> rows.map { it.take(limit) } }.stateIn(backgroundScope)

        assertEquals(listOf(1, 2), pages.value.items)
        assertTrue(pages.value.hasMore)

        window.loadMore()
        assertEquals(listOf(1, 2, 3, 4), pages.value.items)

        window.loadMore()
        assertEquals(listOf(1, 2, 3, 4, 5), pages.value.items)
        assertFalse(pages.value.hasMore)

        window.loadMore()
        assertEquals(6, window.limit.value)
    }

    @Test
    fun `repeated calls before the next page arrive load one page`() = runTest {
        val window = GrowingWindow(pageSize = 2)
        window.observe { limit -> rows.map { it.take(limit) } }.launchIn(backgroundScope)
        runCurrent()

        window.loadMore() // the next page is not collected yet: no runCurrent()
        window.loadMore()
        assertEquals(4, window.limit.value)
    }

    @Test
    fun `new rows stay in the window live`() = runTest(UnconfinedTestDispatcher()) {
        val window = GrowingWindow(pageSize = 3)
        val pages = window.observe { limit -> rows.map { it.take(limit) } }.stateIn(backgroundScope)
        rows.value = listOf(0) + rows.value
        assertEquals(listOf(0, 1, 2), pages.first().items)
    }
}
