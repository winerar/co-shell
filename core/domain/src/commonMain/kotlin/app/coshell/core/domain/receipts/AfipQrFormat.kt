package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AfipReceiptQr
import app.coshell.core.domain.quickentry.ReceiptQr

/** The Argentine receipt QR (#526) as a [ReceiptFormat]; the key stays [ReceiptQr.key] — it is already in saved data. */
object AfipQrFormat : ReceiptFormat {
    override val id = "afip-qr"

    override fun read(text: String): Receipt? {
        if (!text.contains("afip", ignoreCase = true) && !text.contains("eyJ")) return null
        return AfipReceiptQr.find(text)?.toReceipt()
    }
}

fun ReceiptQr.toReceipt(): Receipt =
    Receipt(total = money, date = date, merchant = sellerTaxIdText, key = key, format = AfipQrFormat.id)
