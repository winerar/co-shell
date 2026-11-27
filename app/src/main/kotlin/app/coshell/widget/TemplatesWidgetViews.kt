package app.coshell.widget

import android.content.Context
import android.widget.RemoteViews
import app.coshell.R
import app.coshell.core.designsystem.theme.WidgetColors
import app.coshell.templates.TemplateItem

/** «Шаблоны» (#795): up to three templates and «+» (4×1) or «Все шаблоны ›» (4×2); no templates yet — «Создать». */
internal object TemplatesWidgetViews {
    fun build(context: Context, data: WidgetData, grid: Boolean): RemoteViews {
        val c = data.colors
        val chips = WidgetLayouts.chips(data.templates)
        return when {
            chips.isEmpty() -> empty(context, c)
            grid -> grid(context, c, chips)
            else -> strip(context, c, chips)
        }
    }

    private fun strip(context: Context, c: WidgetColors, items: List<TemplateItem>): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_templates_strip).apply {
            paintCard(c.card, c.cardEdge)
            items.forEachIndexed { i, item -> addView(R.id.t_row, chip(context, c, item, main = i == 0)) }
            addView(R.id.t_row, plus(context, c))
        }

    private fun grid(context: Context, c: WidgetColors, items: List<TemplateItem>): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_templates_grid).apply {
            paintCard(c.card, c.cardEdge)
            // Cells: the templates, then «Все шаблоны ›»; two per row.
            val cells = items.mapIndexed { i, item -> chip(context, c, item, main = i == 0) } + all(context, c)
            cells.forEachIndexed { i, cell -> addView(if (i < 2) R.id.t_row1 else R.id.t_row2, cell) }
        }

    private fun empty(context: Context, c: WidgetColors): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_templates_empty).apply {
            paintCard(c.card, c.cardEdge)
            setTextViewText(R.id.e_text, context.getString(R.string.widget_templates_empty))
            setTextColor(R.id.e_text, c.muted)
            tint(R.id.e_fill, c.quiet)
            tint(R.id.e_icon, c.quietInk)
            setTextViewText(R.id.e_label, context.getString(R.string.widget_templates_create))
            setTextColor(R.id.e_label, c.quietInk)
            setOnClickPendingIntent(R.id.e_create, WidgetIntents.pending(context, "templates_new", screen = "new_template"))
        }

    private fun chip(context: Context, c: WidgetColors, item: TemplateItem, main: Boolean): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_chip).apply {
            val ink = if (main) c.mainInk else c.quietInk
            tint(R.id.c_edge, if (main) c.mainEdge else c.quiet)
            tint(R.id.c_fill, if (main) c.main else c.quiet)
            setTextViewText(R.id.c_name, item.name)
            setTextViewText(R.id.c_amount, WidgetLayouts.amountLine(item, context.getString(R.string.widget_template_ask)))
            setTextColor(R.id.c_name, ink)
            setTextColor(R.id.c_amount, ink)
            setOnClickPendingIntent(R.id.c_root, WidgetIntents.pending(context, "template_${item.id}", templateId = item.id))
        }

    private fun all(context: Context, c: WidgetColors): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_chip).apply {
            tint(R.id.c_edge, c.quiet)
            tint(R.id.c_fill, c.quiet)
            setTextViewText(R.id.c_name, context.getString(R.string.widget_templates_all))
            setTextColor(R.id.c_name, c.quietInk)
            setViewVisibility(R.id.c_amount, android.view.View.GONE)
            setOnClickPendingIntent(R.id.c_root, WidgetIntents.pending(context, "templates_all", screen = "templates"))
        }

    /** «+» — a new expense, like the quick widget. */
    private fun plus(context: Context, c: WidgetColors): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_chip_plus).apply {
            tint(R.id.c_fill, c.quiet)
            tint(R.id.c_icon, c.quietInk)
            setContentDescription(R.id.c_root, context.getString(R.string.shortcut_expense_long))
            setOnClickPendingIntent(R.id.c_root, WidgetIntents.pending(context, "templates_plus", type = "EXPENSE"))
        }
}
