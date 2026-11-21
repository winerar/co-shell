package app.coshell.ai

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** The app side of the «ИИ на телефоне» add-on (#587): the duplicated protocol stays equal; without the add-on nothing breaks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class AddonProtocolTest {
    private val root = File("..").canonicalFile

    @Test
    fun `the protocol file is the same in the app and in the add-on`() {
        fun body(path: String) = File(root, path).readLines().filter { it.trim().startsWith("const val") }
        assertEquals(body("ai-addon/src/main/kotlin/app/coshell/ai/AddonProtocol.kt"), body("app/src/main/kotlin/app/coshell/ai/AddonProtocol.kt"))
    }

    @Test
    fun `the manifests agree on the permission and the package`() {
        val addon = File(root, "ai-addon/src/main/AndroidManifest.xml").readText()
        val app = File(root, "app/src/main/AndroidManifest.xml").readText()
        assertEquals(true, addon.contains("android:name=\"${AddonProtocol.PERMISSION}\""))
        assertEquals(true, addon.contains("android:protectionLevel=\"signature\""))
        assertEquals(true, addon.contains("android:permission=\"${AddonProtocol.PERMISSION}\""))
        assertEquals(true, app.contains("<uses-permission android:name=\"${AddonProtocol.PERMISSION}\""))
        assertEquals(true, app.contains("<package android:name=\"${AddonProtocol.PACKAGE}\""))
    }

    @Test
    fun `without the add-on the model is unavailable and answers nothing`() = runTest {
        val model = AddonLanguageModel(ApplicationProvider.getApplicationContext())
        assertFalse(model.available)
        val status = model.refresh()
        assertFalse(status.installed)
        assertFalse(model.available)
        assertNull(model.complete("system", "prompt"))
    }
}
