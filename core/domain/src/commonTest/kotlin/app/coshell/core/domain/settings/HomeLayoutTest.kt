package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeLayoutTest {
    @Test
    fun `hidden cards come in the order of the layout`() {
        val layout = HomeLayout().move(5, 0).withShown(HomeCard.TILES, false).withShown(HomeCard.REVIEW, false)
        assertEquals(listOf(HomeCard.REVIEW, HomeCard.TILES), layout.hiddenCards)
        assertEquals(emptyList(), HomeLayout().hiddenCards)
    }

    @Test
    fun `a restored card leaves the hidden list and keeps its place`() {
        val hidden = HomeLayout().withShown(HomeCard.UPCOMING, false)
        val back = hidden.withShown(HomeCard.UPCOMING, true)
        assertEquals(emptyList(), back.hiddenCards)
        assertEquals(hidden.order, back.order)
        assertEquals(HomeCard.UPCOMING, back.visible[1])
    }
}
