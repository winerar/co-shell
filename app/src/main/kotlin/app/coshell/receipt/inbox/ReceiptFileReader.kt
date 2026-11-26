package app.coshell.receipt.inbox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.coshell.core.domain.receipts.ReceiptFormats
import app.coshell.receipt.document.PdfText
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Turns a shared file into text for [ReceiptFormats]: a PDF gives its text layer, and when that is no known receipt,
 * the QR codes of its first pages (AFIP invoices print one); a picture gives its QR. Offline, nothing leaves the phone.
 * The file is kept as [SharedFile] in the cache (the newest few) — to attach to «Формат чека» feedback when unread.
 */
@Singleton
class ReceiptFileReader @Inject constructor(@ApplicationContext private val context: Context) {

    /** The file's text for the formats (may be empty) and its kept copy (`null` — could not be read at all). */
    data class Read(val text: String, val file: SharedFile?)

    suspend fun read(uri: String, mime: String?): Read = withContext(Dispatchers.IO) {
        val kept = keep(Uri.parse(uri), mime) ?: return@withContext Read("", null)
        val text = try {
            when {
                kept.mime == Incoming.PDF -> pdf(kept.file)
                kept.mime.startsWith("image/") -> image(kept.file).orEmpty()
                else -> kept.file.readText().take(ReceiptFormats.MAX_TEXT)
            }
        } catch (_: IOException) {
            ""
        } catch (_: SecurityException) {
            ""
        } catch (_: OutOfMemoryError) {
            ""
        }
        Read(text, kept)
    }

    private fun pdf(file: File): String {
        val layer = runCatching { PdfText.extract(file.readBytes()) }.getOrDefault("")
        if (ReceiptFormats.readExact(layer) != null) return layer
        val qr = pdfQr(file)
        return listOfNotNull(layer.ifBlank { null }, qr).joinToString("\n")
    }

    /** Renders up to [PDF_QR_PAGES] pages about [RENDER_WIDTH] px wide and reads the first QR found. */
    private fun pdfQr(file: File): String? =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { renderer ->
                (0 until minOf(renderer.pageCount, PDF_QR_PAGES)).firstNotNullOfOrNull { index ->
                    renderer.openPage(index).use { page ->
                        val height = (RENDER_WIDTH.toLong() * page.height / page.width.coerceAtLeast(1)).toInt().coerceIn(1, RENDER_WIDTH * 2)
                        val bitmap = Bitmap.createBitmap(RENDER_WIDTH, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        QrInImage.read(bitmap).also { bitmap.recycle() }
                    }
                }
            }
        }

    /** Decoded no larger than [IMAGE_MAX] px on the long side: enough for a QR, light on memory. */
    private fun image(file: File): String? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= IMAGE_MAX) sample *= 2
        val bitmap = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        return QrInImage.read(bitmap).also { bitmap.recycle() }
    }

    /** Shared text nothing could read, kept as a `.txt` file — a sample for «Формат чека» feedback. */
    suspend fun keepText(text: String): SharedFile? = withContext(Dispatchers.IO) {
        try {
            val target = File(sharedDir(), "${System.currentTimeMillis()}-$TEXT_NAME")
            target.writeText(text.take(ReceiptFormats.MAX_TEXT))
            prune()
            SharedFile(target, TEXT_NAME, TEXT_MIME)
        } catch (_: IOException) {
            null
        }
    }

    private fun sharedDir() = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }

    private fun prune() {
        sharedDir().listFiles()?.sortedByDescending { it.name }?.drop(KEEP)?.forEach { it.delete() }
    }

    /** Copies the shared file into the cache (the sender's permission ends with the share), keeping the newest [KEEP]. */
    private fun keep(uri: Uri, mime: String?): SharedFile? {
        val resolver = context.contentResolver
        val type = mime ?: resolver.getType(uri) ?: return null
        val dir = sharedDir()
        val name = SharedFile.displayName(resolver, uri) ?: "receipt"
        val target = File(dir, "${System.currentTimeMillis()}-${SharedFile.safeName(name)}")
        val copied = try {
            resolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> SharedFile.copyLimited(input, output, MAX_BYTES) }
            } ?: false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
        if (!copied) {
            target.delete()
            return null
        }
        prune()
        return SharedFile(target, name, type)
    }

    private companion object {
        const val SHARED_DIR = "shared"
        const val TEXT_NAME = "shared-text.txt"
        const val TEXT_MIME = "text/plain"
        const val KEEP = 5
        const val MAX_BYTES = 10L * 1024 * 1024
        const val PDF_QR_PAGES = 2
        const val RENDER_WIDTH = 1600
        const val IMAGE_MAX = 2000
    }
}
