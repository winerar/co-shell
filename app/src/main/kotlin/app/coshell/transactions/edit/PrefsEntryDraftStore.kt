package app.coshell.transactions.edit

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.quickentry.EntryDraft
import app.coshell.core.domain.quickentry.EntryDraftStore
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/** [EntryDraftStore] in a small SharedPreferences file (#323), like `PrefsNoteStore`: per phone, not synced, not in the backup. */
@Singleton
class PrefsEntryDraftStore @Inject constructor(@ApplicationContext context: Context) : EntryDraftStore {
    private val prefs by lazy { context.getSharedPreferences("entry_draft", Context.MODE_PRIVATE) }

    override fun load(): EntryDraft? = runCatching {
        if (!prefs.contains(SAVED_AT)) return null
        EntryDraft(
            type = TransactionType.valueOf(prefs.getString(TYPE, null) ?: return null),
            accountId = prefs.getString(ACCOUNT, null),
            amount = if (prefs.contains(AMOUNT)) Money(prefs.getLong(AMOUNT, 0), CurrencyCode(prefs.getString(CURRENCY, null) ?: return null)) else null,
            categoryId = prefs.getString(CATEGORY, null),
            note = prefs.getString(NOTE, "").orEmpty(),
            tagIds = prefs.getString(TAGS, "").orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
            savedAt = Instant.fromEpochMilliseconds(prefs.getLong(SAVED_AT, 0)),
        )
    }.getOrNull()

    override fun save(draft: EntryDraft) = prefs.edit {
        clear()
        putString(TYPE, draft.type.name)
        draft.accountId?.let { putString(ACCOUNT, it) }
        draft.amount?.let { putLong(AMOUNT, it.amountMinor); putString(CURRENCY, it.currency.value) }
        draft.categoryId?.let { putString(CATEGORY, it) }
        putString(NOTE, draft.note)
        putString(TAGS, draft.tagIds.joinToString(SEPARATOR))
        putLong(SAVED_AT, draft.savedAt.toEpochMilliseconds())
    }

    override fun clear() = prefs.edit { clear() }

    private companion object {
        const val TYPE = "type"
        const val ACCOUNT = "account"
        const val AMOUNT = "amount"
        const val CURRENCY = "currency"
        const val CATEGORY = "category"
        const val NOTE = "note"
        const val TAGS = "tags"
        const val SAVED_AT = "saved_at"
        const val SEPARATOR = "\u0001"
    }
}
