package app.coshell.shortcuts

import app.coshell.core.model.TransactionType

/** Where a launcher shortcut (#555), a widget or the tile (#795) leads: the entry sheet of a type, filled from a template, or a templates screen. */
sealed interface ShortcutLink {
    data class NewOperation(val type: TransactionType) : ShortcutLink
    data class FromTemplate(val templateId: String) : ShortcutLink

    /** «Все шаблоны» (a widget's last chip). */
    data object Templates : ShortcutLink

    /** The new-template editor (the widget's «Создать» when there are no templates yet). */
    data object NewTemplate : ShortcutLink

    companion object {
        const val EXTRA_TYPE = "shortcut_type"
        const val EXTRA_TEMPLATE = "shortcut_template_id"
        const val EXTRA_SCREEN = "shortcut_screen"
        const val SCREEN_TEMPLATES = "templates"
        const val SCREEN_NEW_TEMPLATE = "new_template"

        /** A template wins over a screen, a screen over a type; unknown or blank values lead nowhere (`null`). */
        fun of(type: String?, templateId: String?, screen: String? = null): ShortcutLink? {
            if (!templateId.isNullOrBlank()) return FromTemplate(templateId)
            when (screen) {
                SCREEN_TEMPLATES -> return Templates
                SCREEN_NEW_TEMPLATE -> return NewTemplate
            }
            val parsed = TransactionType.entries.firstOrNull { it.name == type } ?: return null
            return NewOperation(parsed)
        }
    }
}
