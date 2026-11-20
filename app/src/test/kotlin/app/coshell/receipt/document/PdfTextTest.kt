package app.coshell.receipt.document

import app.coshell.core.domain.receipts.ReceiptFormats
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PDFs built here the way generators write them: a Type0 font with Identity-H codes and a `/ToUnicode` CMap, words placed
 * one by one with `Td` inside nested `cm` (as the Teleshka receipt from Telegram), and a plain Latin font with `TJ` spacing.
 */
class PdfTextTest {
    /** Each character gets its own two-byte code; the CMap maps the codes back. */
    private class Codes(text: String) {
        val chars = text.toSet().sorted()
        fun hex(word: String) = word.map { "%04X".format(chars.indexOf(it) + 1) }.joinToString("")
        fun cmap() = buildString {
            append("/CIDInit/ProcSet findresource begin 12 dict begin begincmap\n1 begincodespacerange <0000> <FFFF> endcodespacerange\n")
            append("${chars.size} beginbfchar\n")
            chars.forEachIndexed { i, c -> append("<%04X> <%04X>\n".format(i + 1, c.code)) }
            append("endbfchar endcmap CMapName currentdict /CMap defineresource pop end end")
        }
    }

    private fun deflate(bytes: ByteArray): ByteArray {
        val deflater = Deflater().apply { setInput(bytes); finish() }
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer))
        return out.toByteArray()
    }

    private fun pdf(content: String, cmap: String): ByteArray {
        val out = ByteArrayOutputStream()
        fun w(s: String) = out.write(s.toByteArray(Charsets.ISO_8859_1))
        fun stream(n: Int, data: ByteArray) {
            w("$n 0 obj\n<</Filter/FlateDecode/Length ${data.size}>>stream\n"); out.write(data); w("\nendstream\nendobj\n")
        }
        w("%PDF-1.7\n")
        w("1 0 obj\n<</Type/Pages/Kids[4 0 R]/Count 1>>\nendobj\n")
        w("2 0 obj\n<</Type/Catalog/Pages 1 0 R>>\nendobj\n")
        w("4 0 obj\n<</Type/Page/Parent 1 0 R/Resources<</Font<</F1 6 0 R/F2 7 0 R>>>>/Contents 5 0 R>>\nendobj\n")
        stream(5, deflate(content.toByteArray(Charsets.ISO_8859_1)))
        w("6 0 obj\n<</Type/Font/Subtype/Type0/Encoding/Identity-H/ToUnicode 8 0 R/DescendantFonts[<</W[1 [600 600 600]] /DW 600>>]>>\nendobj\n")
        w("7 0 obj\n<</Type/Font/Subtype/Type1/BaseFont/Helvetica/FirstChar 32/Widths[278]>>\nendobj\n")
        stream(8, deflate(cmap.toByteArray(Charsets.ISO_8859_1)))
        w("trailer\n<</Root 2 0 R>>\n%%EOF")
        return out.toByteArray()
    }

    /** One line of words: each word its own `BT … Td … TJ ET`, columns far apart. */
    private fun row(codes: Codes, y: Int, vararg cells: Pair<Int, String>) = cells.joinToString(" ") { (x, cell) ->
        var at = x
        cell.split(' ').joinToString(" ") { word ->
            "BT /F1 10 Tf $at $y Td [<${codes.hex(word)}>]TJ ET".also { at += word.length * 6 + 3 }
        }
    }

    @Test
    fun `words placed one by one come back as lines with columns`() {
        val lines = listOf(
            listOf(0 to "Teleshka", 400 to "ЧЕК"),
            listOf(400 to "#4807"),
            listOf(0 to "Дата", 150 to "14.11.2026 09:42"),
            listOf(0 to "Пельмени Сибирские, 1кг", 330 to "x1", 400 to "$25.000"),
            listOf(300 to "Итого", 400 to "$82.400"),
        )
        val codes = Codes(lines.flatten().joinToString("") { it.second })
        val body = lines.mapIndexed { i, cells -> row(codes, 700 - i * 20, *cells.toTypedArray()) }.joinToString("\n")
        val text = PdfText.extract(pdf("q 1 0 0 1 40 44 cm q 1 0 0 1 0 10 cm\n$body\nQ Q", codes.cmap()))

        assertEquals(
            "Teleshka\tЧЕК\n#4807\nДата\t14.11.2026 09:42\nПельмени Сибирские, 1кг\tx1\t$25.000\nИтого\t$82.400",
            text,
        )
        assertEquals("teleshka:4807", ReceiptFormats.read(text)?.key)
    }

    @Test
    fun `a Latin font with TJ spacing keeps words apart`() {
        val text = PdfText.extract(pdf("BT /F2 12 Tf 50 700 Td [(Total)-300($)5(1.500)] TJ ET", Codes("").cmap()))
        assertEquals("Total \$1.500", text)
    }

    @Test
    fun `junk is not a PDF`() {
        assertTrue(PdfText.extract("not a pdf at all".toByteArray()).isEmpty())
        assertTrue(PdfText.extract(ByteArray(0)).isEmpty())
    }
}
