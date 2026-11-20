package app.coshell.receipt.document

/**
 * The PDF object model, just enough to read text (#receipts): numbers, names, strings, arrays, dictionaries and
 * references. Parsed by [PdfLexer]; [PdfFile] resolves references.
 */
internal sealed interface PdfValue

internal data class PdfNumber(val value: Double) : PdfValue {
    val int: Int get() = value.toInt()
}

internal data class PdfName(val name: String) : PdfValue

internal class PdfString(val bytes: ByteArray) : PdfValue

internal data class PdfArray(val items: List<PdfValue>) : PdfValue

internal data class PdfDict(val entries: Map<String, PdfValue>) : PdfValue {
    operator fun get(key: String): PdfValue? = entries[key]
}

internal data class PdfRef(val number: Int) : PdfValue

/** An operator of a content stream (`Tj`, `cm`) or a keyword (`true`, `null`, `obj`). */
internal data class PdfOperator(val name: String) : PdfValue

/** A parsed object: its dictionary (or other value) and, for a stream, the decoded data (`null` — unknown filter). */
internal class PdfObject(val value: PdfValue, val stream: ByteArray? = null) {
    val dict: PdfDict? get() = value as? PdfDict
}
