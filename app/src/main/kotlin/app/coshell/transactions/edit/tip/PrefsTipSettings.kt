package app.coshell.transactions.edit.tip

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.quickentry.TipPrefs
import app.coshell.core.domain.quickentry.TipSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [TipSettings] in a small SharedPreferences file of its own (device-only, not synced). */
@Singleton
class PrefsTipSettings @Inject constructor(@ApplicationContext context: Context) : TipSettings {

    private val prefs by lazy { context.getSharedPreferences("tip_settings", Context.MODE_PRIVATE) }

    override suspend fun load(): TipPrefs = withContext(Dispatchers.IO) {
        TipPrefs(categoryId = prefs.getString(CATEGORY, null), accountId = prefs.getString(ACCOUNT, null))
    }

    override suspend fun save(prefs: TipPrefs) = withContext(Dispatchers.IO) {
        this@PrefsTipSettings.prefs.edit {
            putString(CATEGORY, prefs.categoryId)
            putString(ACCOUNT, prefs.accountId)
        }
    }

    private companion object {
        const val CATEGORY = "category"
        const val ACCOUNT = "account"
    }
}
