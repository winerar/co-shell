package app.coshell.home

import app.coshell.core.domain.settings.HomeCard
import app.coshell.core.domain.settings.HomeLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeEditTest {
    private val sizes = mapOf(
        HomeCard.DAILY to 160f, HomeCard.REVIEW to 200f, HomeCard.UPCOMING to 300f, HomeCard.TILES to 200f, HomeCard.CONVERTER to 70f, HomeCard.HOT to 150f, HomeCard.RECENT to 400f, HomeCard.GOALS to 180f,
    )

    @Test
    fun `a small drag changes nothing`() {
        assertEquals(HomeLayout(), HomeLayout().moveBy(HomeCard.UPCOMING, 40f, sizes))
        assertEquals(HomeLayout(), HomeLayout().moveBy(HomeCard.UPCOMING, -40f, sizes))
    }

    @Test
    fun `dragged down past half of the next card, it takes its place`() {
        val moved = HomeLayout().moveBy(HomeCard.UPCOMING, 150f, sizes)
        assertEquals(listOf(HomeCard.DAILY, HomeCard.REVIEW, HomeCard.TILES, HomeCard.UPCOMING, HomeCard.CONVERTER, HomeCard.HOT, HomeCard.RECENT, HomeCard.GOALS), moved.order)
    }

    @Test
    fun `dragged up far enough it goes over two cards`() {
        val moved = HomeLayout().moveBy(HomeCard.TILES, -450f, sizes)
        assertEquals(listOf(HomeCard.DAILY, HomeCard.TILES, HomeCard.REVIEW, HomeCard.UPCOMING, HomeCard.CONVERTER, HomeCard.HOT, HomeCard.RECENT, HomeCard.GOALS), moved.order)
    }

    @Test
    fun `it never leaves the list and hidden cards are skipped over`() {
        val hidden = HomeLayout().withShown(HomeCard.TILES, false)
        val down = hidden.moveBy(HomeCard.REVIEW, 100_000f, sizes)
        assertEquals(listOf(HomeCard.DAILY, HomeCard.UPCOMING, HomeCard.TILES, HomeCard.CONVERTER, HomeCard.HOT, HomeCard.RECENT, HomeCard.GOALS, HomeCard.REVIEW), down.order)
        assertEquals(setOf(HomeCard.TILES), down.hidden)
        val up = hidden.moveBy(HomeCard.RECENT, -100_000f, sizes)
        assertEquals(HomeCard.RECENT, up.order.first())
    }

    @Test
    fun `without sizes nothing moves`() {
        assertEquals(HomeLayout(), HomeLayout().moveBy(HomeCard.REVIEW, 500f, emptyMap()))
    }
}
