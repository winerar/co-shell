package app.coshell.categories

import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.domain.DefaultCategories
import app.coshell.core.model.IdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconsTest {
    @Test
    fun `every default category icon is in the icon set`() {
        var n = 0
        val keys = DefaultCategories.create(IdGenerator { "id${n++}" }).mapNotNull { it.icon }.toSet()
        assertTrue(keys.isNotEmpty())
        assertEquals(emptySet<String>(), keys - CategoryIcons.keys.toSet())
    }

    @Test
    fun `icon keys are unique and unknown keys fall back`() {
        assertEquals(CategoryIcons.keys.size, CategoryIcons.keys.toSet().size)
        assertEquals(DesignR.drawable.ic_category, CategoryIcons.drawableFor("no-such-icon"))
        assertEquals(DesignR.drawable.ic_category, CategoryIcons.drawableFor(null))
    }
}
