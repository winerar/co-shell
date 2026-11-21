package app.coshell.drafts

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The text of the quick note (#586): what was typed stays until «Обработать» turns it into drafts. Per phone, not synced. */
interface NoteStore {
    suspend fun text(): String
    suspend fun save(text: String)
}

/** [NoteStore] in a small SharedPreferences file (like `PrefsHintStore`). */
@Singleton
class PrefsNoteStore @Inject constructor(@ApplicationContext context: Context) : NoteStore {
    private val prefs by lazy { context.getSharedPreferences("quick_note", Context.MODE_PRIVATE) }

    override suspend fun text(): String = withContext(Dispatchers.IO) { prefs.getString(TEXT, "").orEmpty() }

    override suspend fun save(text: String) = withContext(Dispatchers.IO) { prefs.edit { putString(TEXT, text) } }

    private companion object {
        const val TEXT = "text"
    }
}
