package app.coshell.receipt.inbox

import android.graphics.Bitmap
import app.coshell.receipt.scan.QrDecoder
import com.google.zxing.RGBLuminanceSource

/** The text of a QR code in a picture (a shared photo, a rendered PDF page), with the camera scanner's [QrDecoder] (#526, #712). */
internal object QrInImage {
    fun read(bitmap: Bitmap): String? {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return QrDecoder().decode(RGBLuminanceSource(bitmap.width, bitmap.height, pixels))
    }
}
