package app.coshell.widget

import android.app.Application
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.widgetColorsFor
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** #821: a launcher reapplies each update onto the old views; the chips must not pile up. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class TemplatesWidgetReapplyTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val data = WidgetData(
        colors = widgetColorsFor(CoShellPalette.Lime, dark = true),
        locked = false,
        templates = listOf("1", "2").map { TemplateItem(id = it, name = "T$it", type = TransactionType.EXPENSE, accountName = "A", amount = "") },
    )

    private fun childrenAfterUpdates(grid: Boolean, row: Int): Int {
        val parent = FrameLayout(context)
        val view = TemplatesWidgetViews.build(context, data, grid).apply(context, parent)
        repeat(3) { TemplatesWidgetViews.build(context, data, grid).reapply(context, view) }
        return view.findViewById<ViewGroup>(row).childCount
    }

    @Test
    fun `the strip keeps one set of chips after updates`() {
        assertEquals(3, childrenAfterUpdates(grid = false, row = R.id.t_row))
    }

    @Test
    fun `the grid keeps one set of chips after updates`() {
        assertEquals(2, childrenAfterUpdates(grid = true, row = R.id.t_row1))
        assertEquals(1, childrenAfterUpdates(grid = true, row = R.id.t_row2))
    }
}
