package app.coshell.receipt.document

/**
 * Runs a page's content stream far enough to know where text lands: graphics state (`q`/`Q`/`cm`), text objects
 * (`BT`/`ET`), positioning (`Td`, `TD`, `Tm`, `T*`, `TL`), spacing (`Tc`, `Tw`, `Tz`) and showing (`Tj`, `TJ`, `'`, `"`).
 * Everything else (paths, colours, images) is skipped. [font] gives the font of a resource name.
 */
internal class PdfContent(private val data: ByteArray, private val font: (String) -> PdfFont) {
    private val chunks = ArrayList<PdfText.Chunk>()
    private val operands = ArrayList<PdfValue>()
    private var ctm = Matrix.IDENTITY
    private val stack = ArrayDeque<Matrix>()
    private var tm = Matrix.IDENTITY
    private var lineStart = Matrix.IDENTITY
    private var current: PdfFont = PdfFont.FALLBACK
    private var size = 1.0
    private var leading = 0.0
    private var charSpacing = 0.0
    private var wordSpacing = 0.0
    private var scale = 1.0

    fun run(): List<PdfText.Chunk> {
        val lexer = PdfLexer(data)
        var steps = 0
        while (steps++ < MAX_TOKENS) {
            val token = lexer.next() ?: break
            if (token is PdfOperator) {
                if (token.name == "BI") skipInlineImage(lexer) else operate(token.name)
                operands.clear()
            } else {
                operands += token
            }
        }
        return chunks
    }

    private fun operate(op: String) {
        when (op) {
            "q" -> if (stack.size < MAX_STACK) stack.addLast(ctm)
            "Q" -> ctm = stack.removeLastOrNull() ?: ctm
            "cm" -> numbers(6)?.let { ctm = Matrix.of(it).times(ctm) }
            "BT" -> { tm = Matrix.IDENTITY; lineStart = Matrix.IDENTITY }
            "Tf" -> {
                (operands.getOrNull(0) as? PdfName)?.let { current = font(it.name) }
                (operands.getOrNull(1) as? PdfNumber)?.let { size = it.value }
            }
            "Td" -> numbers(2)?.let { moveLine(it[0], it[1]) }
            "TD" -> numbers(2)?.let { leading = -it[1]; moveLine(it[0], it[1]) }
            "Tm" -> numbers(6)?.let { tm = Matrix.of(it); lineStart = tm }
            "T*" -> moveLine(0.0, -leading)
            "TL" -> numbers(1)?.let { leading = it[0] }
            "Tc" -> numbers(1)?.let { charSpacing = it[0] }
            "Tw" -> numbers(1)?.let { wordSpacing = it[0] }
            "Tz" -> numbers(1)?.let { scale = it[0] / 100 }
            "Tj" -> (operands.lastOrNull() as? PdfString)?.let { show(listOf(it)) }
            "'" -> { moveLine(0.0, -leading); (operands.lastOrNull() as? PdfString)?.let { show(listOf(it)) } }
            "\"" -> {
                numbers(2)?.let { wordSpacing = it[0]; charSpacing = it[1] }
                moveLine(0.0, -leading)
                (operands.lastOrNull() as? PdfString)?.let { show(listOf(it)) }
            }
            "TJ" -> (operands.lastOrNull() as? PdfArray)?.let { show(it.items) }
        }
    }

    private fun moveLine(tx: Double, ty: Double) {
        lineStart = Matrix(1.0, 0.0, 0.0, 1.0, tx, ty).times(lineStart)
        tm = lineStart
    }

    /** Shows strings and `TJ` adjustments: each glyph moves the text matrix by its width; a big negative adjustment is a space. */
    private fun show(items: List<PdfValue>) {
        val start = tm.times(ctm)
        val text = StringBuilder()
        for (item in items) {
            when (item) {
                is PdfNumber -> {
                    val shift = -item.value / 1000 * size * scale
                    if (-item.value > SPACE_ADJUST && text.isNotEmpty() && !text.endsWith(" ")) text.append(' ')
                    advance(shift)
                }
                is PdfString -> for (code in current.codes(item.bytes)) {
                    text.append(current.text(code))
                    val spacing = charSpacing + if (current.isSpace(code)) wordSpacing else 0.0
                    advance((current.width(code) / 1000 * size + spacing) * scale)
                }
                else -> Unit
            }
        }
        val end = tm.times(ctm)
        val shown = text.toString().replace("\u0000", "")
        if (shown.isNotEmpty()) {
            val height = kotlin.math.abs(size * start.scaleY)
            chunks += PdfText.Chunk(start.e, start.f, maxOf(end.e, start.e), if (height > 0) height else 1.0, shown)
        }
    }

    private fun advance(dx: Double) {
        tm = Matrix(1.0, 0.0, 0.0, 1.0, dx, 0.0).times(tm)
    }

    private fun numbers(count: Int): DoubleArray? {
        if (operands.size < count) return null
        val tail = operands.subList(operands.size - count, operands.size)
        return DoubleArray(count) { (tail[it] as? PdfNumber)?.value ?: return null }
    }

    /** `BI … ID <binary> EI`: the binary part must not be read as tokens. */
    private fun skipInlineImage(lexer: PdfLexer) {
        val text = String(data, Charsets.ISO_8859_1)
        val id = text.indexOf("ID", lexer.pos).takeIf { it >= 0 } ?: return
        val ei = Regex("""\sEI(\s|$)""").find(text, id + 2)?.range?.last ?: data.size
        lexer.pos = minOf(ei + 1, data.size)
    }

    private companion object {
        const val MAX_TOKENS = 2_000_000
        const val MAX_STACK = 64
        /** A `TJ` adjustment wider than this (thousandths of the size) is a word break, as generators write spaces. */
        const val SPACE_ADJUST = 200.0
    }
}

/** An affine matrix `[a b c d e f]` as in PDF; `x.times(y)` applies x first, then y. */
internal data class Matrix(val a: Double, val b: Double, val c: Double, val d: Double, val e: Double, val f: Double) {
    fun times(o: Matrix) = Matrix(
        a * o.a + b * o.c, a * o.b + b * o.d,
        c * o.a + d * o.c, c * o.b + d * o.d,
        e * o.a + f * o.c + o.e, e * o.b + f * o.d + o.f,
    )

    val scaleY: Double get() = kotlin.math.sqrt(c * c + d * d)

    companion object {
        val IDENTITY = Matrix(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
        fun of(v: DoubleArray) = Matrix(v[0], v[1], v[2], v[3], v[4], v[5])
    }
}
