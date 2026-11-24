package app.coshell.access

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.access.AccessStore
import app.coshell.core.domain.access.StoredAccess
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The invitation code on this phone only (SharedPreferences: not synced, not in the backup — a restored copy on another
 * phone must ask for its own code). The installation id is the one the feedback form sends (`feedback/data/AndroidDeviceInfoProvider`).
 */
class PrefsAccessStore(context: Context) : AccessStore {
    private val prefs by lazy { context.getSharedPreferences("access", Context.MODE_PRIVATE) }
    private val installation by lazy { context.getSharedPreferences("installation", Context.MODE_PRIVATE) }

    override suspend fun load(): StoredAccess? = withContext(Dispatchers.IO) {
        val code = prefs.getString(CODE, null) ?: return@withContext null
        StoredAccess(code, prefs.getString(LABEL, "").orEmpty(), prefs.getLong(CHECKED, 0L), prefs.getBoolean(REVOKED, false))
    }

    override suspend fun save(access: StoredAccess) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(CODE, access.code)
            putString(LABEL, access.label)
            putLong(CHECKED, access.checkedAtMs)
            putBoolean(REVOKED, access.revoked)
        }
    }

    override suspend fun installationId(): String = withContext(Dispatchers.IO) {
        installation.getString(ID, null) ?: UUID.randomUUID().toString().also { id -> installation.edit { putString(ID, id) } }
    }

    private companion object {
        const val CODE = "code"
        const val LABEL = "label"
        const val CHECKED = "checked"
        const val REVOKED = "revoked"
        const val ID = "id"
    }
}
