package app.coshell.receipt.scan

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer

/**
 * ZXing with several tries per picture (#712: the scanner wanted a straight, well-lit receipt). One binarizer is not
 * enough for a thermal receipt: [HybridBinarizer] loses faint print in a shadow, [GlobalHistogramBinarizer] copes with
 * an even dim frame; a light-on-dark QR (a screen in dark mode) needs the inverted picture; a small QR in a big frame —
 * its middle half, so the finder squares are bigger. Pure JVM — the camera ([QrAnalyzer]) and pictures
 * (`receipt/inbox/QrInImage`) both use it; `QrDecoderTest`.
 */
internal class QrDecoder {
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE), DecodeHintType.TRY_HARDER to true))
    }

    fun decode(source: LuminanceSource): String? {
        for (candidate in candidates(source)) {
            tryDecode(BinaryBitmap(HybridBinarizer(candidate)))?.let { return it }
            tryDecode(BinaryBitmap(GlobalHistogramBinarizer(candidate)))?.let { return it }
        }
        return null
    }

    private fun candidates(source: LuminanceSource): Sequence<LuminanceSource> = sequence {
        yield(source)
        yield(source.invert())
        if (source.isCropSupported && source.width >= MIN_CROP_SIDE && source.height >= MIN_CROP_SIDE) {
            yield(source.crop(source.width / 4, source.height / 4, source.width / 2, source.height / 2))
        }
    }

    private fun tryDecode(bitmap: BinaryBitmap): String? = try {
        reader.decodeWithState(bitmap).text
    } catch (_: Exception) {
        null // NotFound/Checksum/Format, and ZXing's own slips on odd frames
    } finally {
        reader.reset()
    }

    private companion object {
        const val MIN_CROP_SIDE = 400
    }
}
