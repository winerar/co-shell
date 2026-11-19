package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.datetime.LocalDate

/**
 * What the QR code of an Argentine receipt says (#526). Only the total: the code has no line items.
 * The buyer's document number is deliberately not kept — it can be a personal id.
 */
data class ReceiptQr(
    val money: Money,
    val date: LocalDate,
    val sellerTaxId: String,
    val pointOfSale: Int,
    val voucherType: Int,
    val voucherNumber: Long,
) {
    /** One receipt, one key: used to warn «already recorded» on a second scan. */
    val key: String get() = "$sellerTaxId:$pointOfSale:$voucherType:$voucherNumber"

    /** The seller's CUIT the way it is written on paper: `30-68584975-1`. */
    val sellerTaxIdText: String get() = "${sellerTaxId.take(2)}-${sellerTaxId.substring(2, 10)}-${sellerTaxId.last()}"
}

/**
 * Reads the AFIP receipt link `https://www.afip.gob.ar/fe/qr/?p=<base64 JSON>` found anywhere in [text]
 * (a scan, a shared message, the clipboard). Works offline; the link is never opened.
 *
 * - `importe` is a JSON number, always with a dot as the decimal separator: it is read as text and never
 *   as a floating-point value (more fraction digits than the currency has → not a receipt we trust).
 * - `moneda`: `ARS`/`PES` → ARS, `DOL` → USD, `060` → EUR, any other three letters as written.
 * - Anything else (wrong host, version, broken base64, missing field) → `null`.
 */
object AfipReceiptQr {

    private val LINK = Regex("""https?://(?:www\.)?afip\.gob\.ar/fe/qr/?\?(?:[^\s#]*&)?p=([^&\s#]+)""", RegexOption.IGNORE_CASE)
    /** The payload alone, as the only content of the text — never picked out of some other link. */
    private val BARE = Regex("""^\s*(eyJ[A-Za-z0-9+/_%=-]{20,})\s*$""")
    private val FIELD = Regex(""""(\w+)"\s*:\s*(?:"((?:[^"\\]|\\.)*)"|(-?[0-9][0-9.eE+-]*|true|false|null))""")
    private val AMOUNT = Regex("""\d+(?:\.\d+)?""")
    private val PERCENT = Regex("""%([0-9A-Fa-f]{2})""")

    fun find(text: String): ReceiptQr? {
        val payload = LINK.find(text)?.groupValues?.get(1) ?: BARE.find(text)?.groupValues?.get(1) ?: return null
        return parsePayload(payload)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun parsePayload(raw: String): ReceiptQr? {
        val base64 = unescape(raw).replace('-', '+').replace('_', '/').trimEnd('=')
        val json = try {
            Base64.Default.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL).decode(base64).decodeToString()
        } catch (_: IllegalArgumentException) {
            return null
        }
        val fields = FIELD.findAll(json).associate { it.groupValues[1] to (it.groups[2]?.value ?: it.groupValues[3]) }
        if (fields["ver"] != "1") return null
        val currency = currency(fields["moneda"] ?: return null) ?: return null
        val money = amount(fields["importe"] ?: return null, currency) ?: return null
        val date = try {
            LocalDate.parse(fields["fecha"] ?: return null)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val seller = fields["cuit"]?.takeIf { it.length == 11 && it.all(Char::isDigit) } ?: return null
        return ReceiptQr(
            money = money,
            date = date,
            sellerTaxId = seller,
            pointOfSale = fields["ptoVta"]?.toIntOrNull() ?: return null,
            voucherType = fields["tipoCmp"]?.toIntOrNull() ?: return null,
            voucherNumber = fields["nroCmp"]?.toLongOrNull() ?: return null,
        )
    }

    private fun unescape(text: String): String =
        PERCENT.replace(text) { it.groupValues[1].toInt(16).toChar().toString() }

    private fun currency(code: String): CurrencyCode? = when (val upper = code.uppercase()) {
        "ARS", "PES" -> CurrencyCode("ARS")
        "DOL" -> CurrencyCode("USD")
        "060" -> CurrencyCode("EUR")
        else -> if (upper.length == 3 && upper.all { it in 'A'..'Z' }) CurrencyCode(upper) else null
    }

    private fun amount(text: String, currency: CurrencyCode): Money? {
        if (!AMOUNT.matches(text)) return null
        val digits = Currencies.info(currency).fractionDigits
        val whole = text.substringBefore('.').toLongOrNull() ?: return null
        val fraction = text.substringAfter('.', "").trimEnd('0')
        if (fraction.length > digits) return null
        val minor = fraction.padEnd(digits, '0').takeIf { digits > 0 }?.toLong() ?: 0L
        val money = try {
            Money.ofMajor(whole, minor, currency)
        } catch (_: ArithmeticException) {
            return null
        }
        return money.takeIf { it.amountMinor > 0 }
    }
}
