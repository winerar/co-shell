package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate

/**
 * A merchant payment QR by EMVCo (Mercado Pago, «Transferencias 3.0», banks of Armenia and Russia): `000201…6304ABCD` — fields
 * «id (2 digits) · length (2) · value». Read only when the CRC (tag 63) is right and the QR carries the sum (tag 54, a QR
 * printed for one payment): a static QR of a shop has no sum. Gives the shop name (59), the currency (53, ISO numeric) and
 * the merchant category code (52) for the category. No date in the QR — the day it was read ([today]).
 * Key `emv:<hash of the whole QR>`: the same QR read twice is the same payment.
 */
class EmvQrFormat(private val today: () -> LocalDate) : ReceiptFormat {
    override val id = ID

    override fun read(text: String): Receipt? {
        if (!text.contains("000201")) return null
        // A PDF gives its text layer and then the QR: the QR is a line of its own.
        val payload = text.lineSequence().map { it.trim() }.firstOrNull { it.startsWith("000201") } ?: return null
        if (payload.length > MAX) return null
        val fields = Emv.fields(payload) ?: return null
        if (!Emv.crcOk(payload, fields)) return null
        val currency = fields["53"]?.let { NUMERIC[it] } ?: return null
        val sum = fields["54"]?.let(SUM::find) ?: return null
        val total = ReceiptNumbers.Printed(sum.groupValues[1], sum.groupValues[2], "").money(currency) ?: return null
        val merchant = fields["59"]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val mcc = fields["52"]?.takeIf { it.length == 4 && it.all(Char::isDigit) && it != "0000" }
        return Receipt(total = total, date = today(), merchant = merchant, key = "emv:${Hash.hex(payload)}", format = id, mcc = mcc)
    }

    companion object {
        const val ID = "emv-qr"
        private const val MAX = 512
        private val SUM = Regex("""^(\d{1,13})(?:\.(\d{1,3}))?$""")

        /** ISO 4217 numeric codes of the currencies these QRs use. Unknown — the QR is not read (no guessing). */
        private val NUMERIC = mapOf(
            "032" to "ARS", "051" to "AMD", "643" to "RUB", "840" to "USD", "978" to "EUR", "986" to "BRL",
            "981" to "GEL", "398" to "KZT", "949" to "TRY", "858" to "UYU", "152" to "CLP", "933" to "BYN",
        ).mapValues { CurrencyCode(it.value) }
    }
}

/** EMVCo «id · length · value» fields and the CRC-16/CCITT-FALSE the standard ends with. */
internal object Emv {
    /** Top-level fields by id; `null` when the text is not a whole run of fields. */
    fun fields(payload: String): Map<String, String>? {
        val out = mutableMapOf<String, String>()
        var i = 0
        while (i < payload.length) {
            if (i + 4 > payload.length) return null
            val tag = payload.substring(i, i + 2)
            val len = payload.substring(i + 2, i + 4).toIntOrNull() ?: return null
            if (!tag.all(Char::isDigit) || i + 4 + len > payload.length) return null
            out[tag] = payload.substring(i + 4, i + 4 + len)
            i += 4 + len
        }
        return out
    }

    /** Tag 63 is the last field: the CRC of everything before its value, «6304» included. */
    fun crcOk(payload: String, fields: Map<String, String>): Boolean {
        val crc = fields["63"]?.takeIf { it.length == 4 } ?: return false
        if (!payload.endsWith("6304$crc")) return false
        return crc16(payload.dropLast(4)).equals(crc, ignoreCase = true)
    }

    fun crc16(text: String): String {
        var crc = 0xFFFF
        for (byte in text.encodeToByteArray()) {
            crc = crc xor ((byte.toInt() and 0xFF) shl 8)
            repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1 }
            crc = crc and 0xFFFF
        }
        return crc.toString(16).uppercase().padStart(4, '0')
    }
}

/** FNV-1a 64: a stable short fingerprint of a text for receipt keys (same text — same key, on every platform). */
internal object Hash {
    fun hex(text: String): String {
        var h = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
        for (byte in text.encodeToByteArray()) {
            h = h xor (byte.toLong() and 0xFF)
            h *= 0x100000001b3L
        }
        return h.toULong().toString(16).padStart(16, '0')
    }
}
