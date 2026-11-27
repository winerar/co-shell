package app.coshell.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import app.coshell.R
import app.coshell.core.designsystem.theme.WidgetColors

/** «Быстрая запись» (#795, poll 2026-11-27 «Капсулы»): «Расход» and «Доход»; icons only on 2×1; closed entry — muted with a lock line. */
internal object QuickWidgetViews {
    private const val MUTED_ALPHA = 140

    fun build(context: Context, data: WidgetData, mode: QuickMode): RemoteViews {
        val c = data.colors
        val expense = context.getString(R.string.shortcut_expense)
        val income = context.getString(R.string.shortcut_income)
        return when {
            data.locked && mode == QuickMode.WORDS -> locked(context, c, expense, income)
            mode == QuickMode.ICONS -> icons(context, c, expense, income, data.locked)
            else -> words(context, c, expense, income)
        }
    }

    private fun words(context: Context, c: WidgetColors, expense: String, income: String): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_quick_words).apply {
            paintCard(c.card, c.cardEdge)
            paintExpense(c)
            paintIncome(c)
            setTextViewText(R.id.w_expense_text, expense)
            setTextViewText(R.id.w_income_text, income)
            setTextColor(R.id.w_expense_text, c.mainInk)
            setTextColor(R.id.w_income_text, c.quietInk)
            tint(R.id.w_expense_icon, c.mainInk)
            tint(R.id.w_income_icon, c.quietInk)
            setOnClickPendingIntent(R.id.w_expense, WidgetIntents.pending(context, "quick_expense", type = "EXPENSE"))
            setOnClickPendingIntent(R.id.w_income, WidgetIntents.pending(context, "quick_income", type = "INCOME"))
        }

    private fun icons(context: Context, c: WidgetColors, expense: String, income: String, locked: Boolean): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_quick_icons).apply {
            paintCard(c.card, c.cardEdge)
            paintExpense(c, muted = locked)
            paintIncome(c, muted = locked)
            tint(R.id.w_expense_icon, if (locked) c.muted else c.mainInk)
            tint(R.id.w_income_icon, if (locked) c.muted else c.quietInk)
            // Icons only: TalkBack reads the words.
            setContentDescription(R.id.w_expense, expense)
            setContentDescription(R.id.w_income, income)
            setOnClickPendingIntent(R.id.w_expense, WidgetIntents.pending(context, "quick_expense", type = "EXPENSE"))
            setOnClickPendingIntent(R.id.w_income, WidgetIntents.pending(context, "quick_income", type = "INCOME"))
        }

    /** Closed entry: the whole card opens the «Обновить» window (the same path as «+»). */
    private fun locked(context: Context, c: WidgetColors, expense: String, income: String): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_quick_locked).apply {
            paintCard(c.card, c.cardEdge)
            tint(R.id.w_lock_icon, c.muted)
            setTextViewText(R.id.w_lock_text, context.getString(R.string.widget_locked))
            setTextColor(R.id.w_lock_text, c.muted)
            paintExpense(c, muted = true)
            paintIncome(c, muted = true)
            setTextViewText(R.id.w_expense_text, expense)
            setTextViewText(R.id.w_income_text, income)
            setTextColor(R.id.w_expense_text, c.muted)
            setTextColor(R.id.w_income_text, c.muted)
            tint(R.id.w_expense_icon, c.muted)
            tint(R.id.w_income_icon, c.muted)
            setOnClickPendingIntent(R.id.w_root, WidgetIntents.pending(context, "quick_locked", type = "EXPENSE"))
        }

    private fun RemoteViews.paintExpense(c: WidgetColors, muted: Boolean = false) {
        if (muted) {
            tint(R.id.w_expense_edge, quietFill(c))
            tint(R.id.w_expense_fill, quietFill(c))
        } else {
            tint(R.id.w_expense_edge, c.mainEdge)
            tint(R.id.w_expense_fill, c.main)
        }
    }

    private fun RemoteViews.paintIncome(c: WidgetColors, muted: Boolean = false) = tint(R.id.w_income_fill, if (muted) quietFill(c) else c.quiet)

    /** The calm fill, a little fainter: both buttons look closed. */
    private fun quietFill(c: WidgetColors): Int = ColorUtils.setAlphaComponent(c.quiet, MUTED_ALPHA)
}
