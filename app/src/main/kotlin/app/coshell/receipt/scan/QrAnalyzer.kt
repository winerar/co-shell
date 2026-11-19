package app.coshell.receipt.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Reads QR codes from camera frames with ML Kit's bundled model (#526: no Google services needed, offline).
 * Every frame is closed at once; [onText] gets the text of the first QR in the frame, on the main thread.
 */
internal class QrAnalyzer(private val onText: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(frame: ImageProxy) {
        val media = frame.image
        if (media == null) {
            frame.close()
            return
        }
        scanner.process(InputImage.fromMediaImage(media, frame.imageInfo.rotationDegrees))
            .addOnSuccessListener { codes -> codes.firstNotNullOfOrNull { it.rawValue }?.let(onText) }
            .addOnCompleteListener { frame.close() }
    }

    fun close() = scanner.close()
}
