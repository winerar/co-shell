package app.coshell.categories

import app.coshell.core.domain.CategoryNames
import app.coshell.core.domain.DefaultCategories
import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceCategoryNamesTest {
    @Test
    fun `every category the app creates has a string resource`() {
        val needed = DefaultCategories.keys.toSet() + CategoryNames.ADJUSTMENT
        assertEquals(emptySet<String>(), needed - ResourceCategoryNames.IDS.keys)
        assertEquals(emptySet<String>(), ResourceCategoryNames.IDS.keys - needed)
    }
}
