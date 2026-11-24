package app.coshell.receipt.scan

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.PlanarYUVLuminanceSource

/**
 * Reads QR codes from camera frames with ZXing (#526: pure Java, offline, no Google services; ML Kit cost ~10 MB of APK).
 * Only the brightness plane is read; a QR is found at any rotation, with the tries of [QrDecoder] (#712). Every frame
 * is closed at once; [onText] gets the text of the first QR found, from the analysis thread.
 */
internal class QrAnalyzer(private val onText: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val decoder = QrDecoder()

    override fun analyze(frame: ImageProxy) {
        try {
            val plane = frame.planes[0]
            val buffer = plane.buffer
            val data = ByteArray(buffer.remaining()).also { buffer.get(it) }
            val rowStride = plane.rowStride
            val height = frame.height
            val source = PlanarYUVLuminanceSource(data, rowStride, height, 0, 0, minOf(frame.width, rowStride), height, false)
            decoder.decode(source)?.let(onText)
        } finally {
            frame.close()
        }
    }

    fun close() = Unit
}
