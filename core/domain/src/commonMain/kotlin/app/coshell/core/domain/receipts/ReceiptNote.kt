package app.coshell.core.domain.receipts

import app.coshell.core.model.Money

/**
 * The note an operation gets from a receipt with line items (owner 2026-11-20: «список покупок — сразу в заметку»):
 * the shop on the first line (what the list row shows), then one line per item — «Пельмени Сибирские, 1кг — 25 000 ARS»,
 * «×2» only when more than one. Search finds the operation by any product. `null` — no items (a QR has none).
 * Cut at [MAX] characters on a line boundary, with «…» when items were left out.
 */
object ReceiptNote {
    const val MAX = 1_000

    fun of(receipt: Receipt, format: (Money) -> String): String? {
        if (receipt.items.isEmpty()) return null
        val lines = receipt.items.map { item ->
            val count = item.quantity.takeUnless { it == "1" }?.let { " ×$it" }.orEmpty()
            "${item.name}$count — ${format(item.amount)}"
        }
        val out = StringBuilder(receipt.merchant)
        for (line in lines) {
            if (out.length + 1 + line.length > MAX - ELLIPSIS.length) {
                out.append('\n').append(ELLIPSIS)
                break
            }
            out.append('\n').append(line)
        }
        return out.toString()
    }

    private const val ELLIPSIS = "…"
}
