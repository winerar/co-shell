package app.coshell.core.domain.receipts

/**
 * One known receipt format. Every source is reduced to text first (QR content, a link, a shared message, the text
 * layer of a PDF), so a format only reads text. [read] must be cheap on foreign text: check a marker with
 * `contains` before any regex, return `null` for anything it does not fully trust (no guessing).
 */
interface ReceiptFormat {
    /** Short stable id: part of the dedupe key and of the «Формат чека» feedback. */
    val id: String

    fun read(text: String): Receipt?
}

/**
 * All known formats, tried in order; the first that reads the text wins. A new format = an object here + a test
 * with an anonymised sample in `docs/samples/receipts/` (`docs/agents/receipts.md`).
 */
object ReceiptFormats {
    val all: List<ReceiptFormat> = listOf(AfipQrFormat, TeleshkaFormat, AmeriabankTransferFormat, AmeriabankFormat, TbankFormat)

    /** Texts longer than this are not receipts (a whole book shared by mistake); read only the head. */
    const val MAX_TEXT = 20_000

    fun read(text: String): Receipt? {
        val head = if (text.length > MAX_TEXT) text.take(MAX_TEXT) else text
        return all.firstNotNullOfOrNull { it.read(head) }
    }
}
