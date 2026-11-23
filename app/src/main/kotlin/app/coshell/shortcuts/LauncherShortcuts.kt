package app.coshell.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.coshell.R
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateCatalog
import app.coshell.templates.TemplateItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Launcher shortcuts (#555): «Расход», «Доход», «Перевод» and the two most used templates («Кофе · 3 500 ARS»).
 * All are dynamic (a static `shortcuts.xml` cannot name the debug package); templates follow their usage.
 */
class LauncherShortcuts @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalog: TemplateCatalog,
) {
    suspend fun run() {
        catalog.observe()
            .map { snapshot -> pick(snapshot.items, snapshot.counts) }
            .distinctUntilChanged()
            .collect { publish(it) }
    }

    private fun publish(items: List<TemplateItem>) {
        val templates = items.map { item ->
            shortcut("template_${item.id}", label(item, context.getString(R.string.shortcut_template_label)), R.drawable.ic_shortcut_template, ShortcutLinks.intent(context, templateId = item.id))
        }
        val types = listOf(
            Triple(TransactionType.EXPENSE, R.string.shortcut_expense, R.drawable.ic_shortcut_expense),
            Triple(TransactionType.INCOME, R.string.shortcut_income, R.drawable.ic_shortcut_income),
            Triple(TransactionType.TRANSFER, R.string.shortcut_transfer, R.drawable.ic_shortcut_transfer),
        ).map { (type, text, icon) -> shortcut("type_${type.name}", context.getString(text), icon, ShortcutLinks.intent(context, type = type.name)) }
        // The launcher shows the first few by rank: the types, then the templates.
        val all = types + templates
        runCatching { ShortcutManagerCompat.setDynamicShortcuts(context, all) }
    }

    private fun shortcut(id: String, label: String, icon: Int, intent: Intent) =
        ShortcutInfoCompat.Builder(context, id).setShortLabel(label).setIcon(IconCompat.createWithResource(context, icon)).setIntent(intent).build()

    companion object {
        const val LIMIT = 2

        /** Used at least once, most used first. */
        fun pick(items: List<TemplateItem>, counts: Map<String, Int>): List<TemplateItem> =
            items.filter { (counts[it.id] ?: 0) > 0 }.sortedByDescending { counts[it.id] }.take(LIMIT)

        /** [format] — `%1$s · %2$s` (name, amount) from the resources. */
        fun label(item: TemplateItem, format: String): String =
            if (item.amount.isBlank()) item.name else format.format(item.name, item.amount)
    }
}
