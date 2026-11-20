package app.coshell.receipt.document

/**
 * What a font says about its text: how codes map to Unicode (`/ToUnicode` CMap; without one, single bytes are read
 * as Latin-1) and how wide each glyph is (to tell a space between words from a gap between columns).
 * Type0 fonts use two-byte codes (Identity-H, as most generated PDFs do).
 */
internal class PdfFont(
    private val twoByte: Boolean,
    private val unicode: Map<Int, String>,
    private val widths: Map<Int, Double>,
    private val defaultWidth: Double,
) {
    /** Codes of [bytes] in this font. */
    fun codes(bytes: ByteArray): List<Int> = if (twoByte) {
        List(bytes.size / 2) { i -> ((bytes[i * 2].toInt() and 0xFF) shl 8) or (bytes[i * 2 + 1].toInt() and 0xFF) }
    } else {
        bytes.map { it.toInt() and 0xFF }
    }

    fun text(code: Int): String = unicode[code] ?: if (twoByte) "" else code.toChar().toString()

    /** Glyph width in text space units (1/1000 of the font size). */
    fun width(code: Int): Double = widths[code] ?: defaultWidth

    fun isSpace(code: Int): Boolean = !twoByte && code == SPACE

    companion object {
        private const val SPACE = 32
        val FALLBACK = PdfFont(twoByte = false, unicode = emptyMap(), widths = emptyMap(), defaultWidth = 500.0)

        fun of(dict: PdfDict, file: PdfFile): PdfFont {
            val twoByte = (dict["Subtype"] as? PdfName)?.name == "Type0"
            val unicode = file.obj(dict["ToUnicode"])?.stream?.let(::parseCMap).orEmpty()
            return if (twoByte) {
                val descendant = file.dict((file.resolve(dict["DescendantFonts"]) as? PdfArray)?.items?.firstOrNull())
                val default = (file.resolve(descendant?.get("DW")) as? PdfNumber)?.value ?: 1000.0
                PdfFont(true, unicode, cidWidths(file.resolve(descendant?.get("W")) as? PdfArray, file), default)
            } else {
                val first = (file.resolve(dict["FirstChar"]) as? PdfNumber)?.int ?: 0
                val list = (file.resolve(dict["Widths"]) as? PdfArray)?.items.orEmpty()
                val widths = list.mapIndexedNotNull { i, w -> (file.resolve(w) as? PdfNumber)?.let { first + i to it.value } }.toMap()
                PdfFont(false, unicode, widths, 500.0)
            }
        }

        /** `/W [c [w1 w2 …]  cFirst cLast w …]`, any part may be a reference. */
        private fun cidWidths(array: PdfArray?, file: PdfFile): Map<Int, Double> {
            val items = array?.items?.map { file.resolve(it) } ?: return emptyMap()
            val out = HashMap<Int, Double>()
            var i = 0
            while (i < items.size) {
                val start = (items[i] as? PdfNumber)?.int ?: break
                when (val next = items.getOrNull(i + 1)) {
                    is PdfArray -> { next.items.forEachIndexed { k, w -> (file.resolve(w) as? PdfNumber)?.let { out[start + k] = it.value } }; i += 2 }
                    is PdfNumber -> {
                        val width = (items.getOrNull(i + 2) as? PdfNumber)?.value ?: break
                        if (next.int - start in 0..MAX_RANGE) for (c in start..next.int) out[c] = width
                        i += 3
                    }
                    else -> break
                }
            }
            return out
        }

        /** `beginbfchar <src> <dst> … endbfchar` and `beginbfrange <lo> <hi> <dst>|[<d>…] … endbfrange`. */
        fun parseCMap(data: ByteArray): Map<Int, String> {
            val lexer = PdfLexer(data)
            val out = HashMap<Int, String>()
            while (true) {
                when ((lexer.next() ?: break).let { (it as? PdfOperator)?.name }) {
                    "beginbfchar" -> while (true) {
                        val src = lexer.next() as? PdfString ?: break
                        val dst = lexer.next() as? PdfString ?: break
                        out[src.code()] = dst.utf16()
                    }
                    "beginbfrange" -> while (true) {
                        val lo = lexer.next() as? PdfString ?: break
                        val hi = lexer.next() as? PdfString ?: break
                        val from = lo.code()
                        val to = hi.code()
                        if (to - from !in 0..MAX_RANGE) break
                        when (val dst = lexer.next()) {
                            is PdfString -> {
                                val base = dst.utf16()
                                for (c in from..to) out[c] = base.dropLast(1) + (base.last() + (c - from))
                            }
                            is PdfArray -> dst.items.forEachIndexed { k, d -> (d as? PdfString)?.let { out[from + k] = it.utf16() } }
                            else -> break
                        }
                    }
                }
            }
            return out
        }

        private const val MAX_RANGE = 0xFFFF

        private fun PdfString.code(): Int = bytes.fold(0) { acc, b -> (acc shl 8) or (b.toInt() and 0xFF) }

        private fun PdfString.utf16(): String =
            if (bytes.isEmpty()) "\u0000" else String(bytes, Charsets.UTF_16BE).ifEmpty { "\u0000" }
    }
}
