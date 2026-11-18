package app.coshell.family.sync

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import app.coshell.settings.PrefsScreenOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class SyncNetworkTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun wifiOnly(on: Boolean) =
        context.getSharedPreferences(PrefsScreenOptions.FILE, Context.MODE_PRIVATE).edit(commit = true) { putBoolean(PrefsScreenOptions.KEY_SYNC_WIFI, on) }

    @Test
    fun `any network by default`() {
        wifiOnly(false)
        assertFalse(SyncNetwork.wifiOnly(context))
        assertEquals(NetworkType.CONNECTED, SyncNetwork.constraints(context).requiredNetworkType)
        assertTrue(SyncNetwork.allowedNow(context))
    }

    @Test
    fun `wifi only asks the workers for an unmetered network`() {
        wifiOnly(true)
        assertTrue(SyncNetwork.wifiOnly(context))
        assertEquals(NetworkType.UNMETERED, SyncNetwork.constraints(context).requiredNetworkType)
    }
}
