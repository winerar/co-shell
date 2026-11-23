package app.coshell.shortcuts

import app.coshell.core.model.TransactionType

/** Where a launcher shortcut (#555) leads: the entry sheet of a type, or filled from a template. */
sealed interface ShortcutLink {
    data class NewOperation(val type: TransactionType) : ShortcutLink
    data class FromTemplate(val templateId: String) : ShortcutLink

    companion object {
        const val EXTRA_TYPE = "shortcut_type"
        const val EXTRA_TEMPLATE = "shortcut_template_id"

        /** A template wins over a type; unknown or blank values lead nowhere (`null`). */
        fun of(type: String?, templateId: String?): ShortcutLink? {
            if (!templateId.isNullOrBlank()) return FromTemplate(templateId)
            val parsed = TransactionType.entries.firstOrNull { it.name == type } ?: return null
            return NewOperation(parsed)
        }
    }
}
