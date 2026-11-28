package app.coshell.newmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every «новое» place deeper inside has a way from «Ещё» (owner 2026-11-28: the pill leads to the very row). */
class NewFeaturesTest {
    private fun ways(place: String): List<String> = NewFeatures.inside.filterValues { place in it }.keys.toList()

    private fun reachesMore(place: String, seen: Set<String> = emptySet()): Boolean =
        place in NewFeatures.more || (place !in seen && ways(place).any { reachesMore(it, seen + place) })

    @Test
    fun `every deeper place is reached from More`() {
        NewFeatures.deeper.forEach { assertTrue(it, reachesMore(it)) }
    }

    @Test
    fun `every way leads somewhere new and starts on the way from More`() {
        NewFeatures.inside.forEach { (way, places) ->
            assertTrue(way, places.isNotEmpty())
            assertTrue(way, reachesMore(way))
        }
    }

    @Test
    fun `only deeper places inherit and only from More`() {
        assertTrue(NewFeatures.deeper.containsAll(NewFeatures.inherits.keys))
        assertTrue(NewFeatures.more.containsAll(NewFeatures.inherits.values))
    }

    @Test
    fun `keys are unique`() {
        assertEquals(NewFeatures.all.size, NewFeatures.all.toSet().size)
    }
}
