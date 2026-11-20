package app.coshell.receipt.document

import java.io.ByteArrayOutputStream

/**
 * Reads PDF values from bytes starting at [pos]: used for object bodies and for content streams (where bare words
 * are operators). Malformed input never throws past [next] — it yields `null` at the end and skips junk bytes.
 */
internal class PdfLexer(private val data: ByteArray, var pos: Int = 0, private val end: Int = data.size) {

    fun next(): PdfValue? {
        skipSpace()
        if (pos >= end) return null
        val c = data[pos].toInt().toChar()
        return when {
            c == '/' -> PdfName(readName())
            c == '(' -> PdfString(readLiteral())
            c == '<' && peek(1) == '<' -> { pos += 2; readDict() }
            c == '<' -> PdfString(readHex())
            c == '[' -> { pos++; readArray() }
            c == ']' || c == '>' || c == ')' || c == '{' || c == '}' -> { pos++; next() }
            c.isDigit() || c == '-' || c == '+' || c == '.' -> readNumberOrRef()
            else -> PdfOperator(readWord())
        }
    }

    private fun readArray(): PdfArray {
        val items = ArrayList<PdfValue>()
        while (true) {
            skipSpace()
            if (pos >= end) break
            if (data[pos].toInt().toChar() == ']') { pos++; break }
            items += next() ?: break
        }
        return PdfArray(items)
    }

    private fun readDict(): PdfDict {
        val map = LinkedHashMap<String, PdfValue>()
        while (true) {
            skipSpace()
            if (pos >= end) break
            if (data[pos].toInt().toChar() == '>' && peek(1) == '>') { pos += 2; break }
            val key = next() as? PdfName ?: continue
            skipSpace()
            if (pos < end && data[pos].toInt().toChar() == '>' && peek(1) == '>') break
            map[key.name] = next() ?: break
        }
        return PdfDict(map)
    }

    /** `12`, `-3.5`, or `12 0 R` — a reference is two integers followed by `R`. */
    private fun readNumberOrRef(): PdfValue {
        val first = readNumber()
        val save = pos
        if (first.value == first.int.toDouble() && first.int >= 0) {
            skipSpace()
            if (pos < end && data[pos].toInt().toChar().isDigit()) {
                val second = readNumber()
                skipSpace()
                if (second.value == 0.0 && pos < end && data[pos].toInt().toChar() == 'R' && !isRegular(peek(1))) {
                    pos++
                    return PdfRef(first.int)
                }
            }
        }
        pos = save
        return first
    }

    private fun readNumber(): PdfNumber {
        val start = pos
        pos++
        while (pos < end && data[pos].toInt().toChar().let { it.isDigit() || it == '.' }) pos++
        return PdfNumber(String(data, start, pos - start, Charsets.ISO_8859_1).toDoubleOrNull() ?: 0.0)
    }

    private fun readName(): String {
        pos++
        val out = StringBuilder()
        while (pos < end && isRegular(data[pos].toInt().toChar())) {
            val c = data[pos].toInt().toChar()
            if (c == '#' && pos + 2 < end) {
                val hex = String(data, pos + 1, 2, Charsets.ISO_8859_1).toIntOrNull(16)
                if (hex != null) { out.append(hex.toChar()); pos += 3; continue }
            }
            out.append(c)
            pos++
        }
        return out.toString()
    }

    private fun readWord(): String {
        val start = pos
        while (pos < end && isRegular(data[pos].toInt().toChar())) pos++
        if (pos == start) pos++ // a stray delimiter: skip it
        return String(data, start, pos - start, Charsets.ISO_8859_1)
    }

    private fun readLiteral(): ByteArray {
        pos++
        val out = ByteArrayOutputStream()
        var depth = 1
        while (pos < end) {
            val b = data[pos++].toInt() and 0xFF
            when (b.toChar()) {
                '(' -> { depth++; out.write(b) }
                ')' -> { if (--depth == 0) break; out.write(b) }
                '\\' -> if (pos < end) escape(out)
                else -> out.write(b)
            }
        }
        return out.toByteArray()
    }

    private fun escape(out: ByteArrayOutputStream) {
        val c = data[pos++].toInt().toChar()
        when (c) {
            'n' -> out.write('\n'.code)
            'r' -> out.write('\r'.code)
            't' -> out.write('\t'.code)
            'b' -> out.write(8)
            'f' -> out.write(12)
            '\r' -> if (pos < end && data[pos].toInt().toChar() == '\n') pos++
            '\n' -> Unit
            in '0'..'7' -> {
                var value = c - '0'
                repeat(2) {
                    if (pos < end && data[pos].toInt().toChar() in '0'..'7') value = value * 8 + (data[pos++].toInt().toChar() - '0')
                }
                out.write(value and 0xFF)
            }
            else -> out.write(c.code)
        }
    }

    private fun readHex(): ByteArray {
        pos++
        val digits = StringBuilder()
        while (pos < end) {
            val c = data[pos++].toInt().toChar()
            if (c == '>') break
            if (c.isLetterOrDigit()) digits.append(c)
        }
        if (digits.length % 2 == 1) digits.append('0')
        return ByteArray(digits.length / 2) { i -> digits.substring(i * 2, i * 2 + 2).toIntOrNull(16)?.toByte() ?: 0 }
    }

    fun skipSpace() {
        while (pos < end) {
            val c = data[pos].toInt().toChar()
            when {
                c == '%' -> while (pos < end && data[pos].toInt().toChar().let { it != '\n' && it != '\r' }) pos++
                c.isWhitespace() || c == '\u0000' -> pos++
                else -> return
            }
        }
    }

    private fun peek(offset: Int): Char = if (pos + offset < end) data[pos + offset].toInt().toChar() else ' '

    private fun isRegular(c: Char): Boolean = !c.isWhitespace() && c != '\u0000' && c !in "()<>[]{}/%"
}
