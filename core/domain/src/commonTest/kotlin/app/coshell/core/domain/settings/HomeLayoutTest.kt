package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeLayoutTest {
    @Test
    fun `by default every card is shown in the order of the app`() {
        val layout = HomeLayout()
        assertEquals(HomeCard.entries, layout.visible)
        assertEquals(5, layout.shownCount)
    }

    @Test
    fun `a hidden card is not drawn, shown again it comes back to its place`() {
        val hidden = HomeLayout().withShown(HomeCard.HOT, false)
        assertEquals(listOf(HomeCard.REVIEW, HomeCard.UPCOMING, HomeCard.TILES, HomeCard.RECENT), hidden.visible)
        assertEquals(HomeCard.entries, hidden.withShown(HomeCard.HOT, true).visible)
    }

    @Test
    fun `a card moves to a new place and the others keep their order`() {
        val moved = HomeLayout().move(from = 4, to = 0)
        assertEquals(listOf(HomeCard.RECENT, HomeCard.REVIEW, HomeCard.UPCOMING, HomeCard.TILES, HomeCard.HOT), moved.order)
        assertEquals(HomeLayout(), HomeLayout().move(2, 2))
        assertEquals(HomeLayout(), HomeLayout().move(2, 9))
    }

    @Test
    fun `the saved text comes back as it was`() {
        val layout = HomeLayout().move(4, 1).withShown(HomeCard.REVIEW, false)
        assertEquals(layout, HomeLayout.parse(layout.encode()))
    }

    @Test
    fun `an old or broken saved text still gives every card, new ones at the end`() {
        assertEquals(HomeLayout(), HomeLayout.parse(null))
        assertEquals(HomeLayout(), HomeLayout.parse("garbage"))
        val old = HomeLayout.parse("RECENT,TILES|REVIEW,GONE")
        assertEquals(listOf(HomeCard.RECENT, HomeCard.TILES, HomeCard.REVIEW, HomeCard.UPCOMING, HomeCard.HOT), old.order)
        assertEquals(setOf(HomeCard.REVIEW), old.hidden)
        assertTrue(HomeCard.RECENT in old.visible)
    }

    @Test
    fun `no card shown leaves only the balance`() {
        val none = HomeCard.entries.fold(HomeLayout()) { l, c -> l.withShown(c, false) }
        assertEquals(emptyList(), none.visible)
    }
}
