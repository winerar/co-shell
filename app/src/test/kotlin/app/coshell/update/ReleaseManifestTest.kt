package app.coshell.update

import app.coshell.update.data.parseReleaseManifest
import app.coshell.update.data.releaseFromJson
import app.coshell.update.data.sha256
import app.coshell.update.data.toJson
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseManifestTest {
    private val hash = "AB".repeat(32)
    private val url = "https://github.com/winerar/co-shell-releases/releases/latest/download/version.json"

    private fun manifest(apk: String = "co-shell-42.apk", sha: String = hash, size: Long = 5_000_000) = """
        {"versionCode": 42, "versionName": "0.2.0", "apk": "$apk", "sha256": "$sha",
         "size": $size, "notes": "  • Новое  ", "extra": true}
    """.trimIndent()

    @Test
    fun `relative apk path is resolved against the manifest url`() {
        val release = parseReleaseManifest(manifest(), url)
        assertEquals(42, release.versionCode)
        assertEquals("0.2.0", release.versionName)
        assertEquals("https://github.com/winerar/co-shell-releases/releases/latest/download/co-shell-42.apk", release.apkUrl)
        assertEquals("ab".repeat(32), release.sha256)
        assertEquals("• Новое", release.notes)
    }

    @Test
    fun `absolute apk url is kept`() {
        val apk = "https://github.com/winerar/co-shell-releases/releases/download/build-42/co-shell-42.apk"
        assertEquals(apk, parseReleaseManifest(manifest(apk = apk), url).apkUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `plain http apk is refused`() {
        parseReleaseManifest(manifest(apk = "http://evil.example/x.apk"), url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `bad checksum is refused`() {
        parseReleaseManifest(manifest(sha = "123"), url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero size is refused`() {
        parseReleaseManifest(manifest(size = 0), url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `not json is refused`() {
        parseReleaseManifest("<html>404</html>", url)
    }

    @Test
    fun `pending release survives a round trip through preferences`() {
        val release = parseReleaseManifest(manifest(), url)
        assertEquals(release, releaseFromJson(release.toJson()))
        assertNull(releaseFromJson("garbage"))
    }

    @Test
    fun `sha256 of a file matches the known value`() {
        val file = File.createTempFile("apk", ".bin").apply { writeText("abc"); deleteOnExit() }
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", file.sha256())
    }
}
