package app.coshell.receipt.document

/**
 * The text layer of a PDF as lines (#receipts: shared receipts from shops and bots), without a PDF library:
 * words are placed by the text and graphics matrices, then grouped into lines top to bottom. Between words on a line —
 * a space; across a wide gap (a table column) — a tab. Scanned PDFs (images only) give an empty text.
 */
internal object PdfText {
    private const val MAX_PAGES = 3

    fun extract(data: ByteArray, maxPages: Int = MAX_PAGES): String {
        val file = PdfFile.parse(data)
        return file.pages(maxPages).joinToString("\n") { (page, resources) ->
            val fonts = HashMap<String, PdfFont>()
            val fontDicts = file.dict(resources?.get("Font"))
            val chunks = PdfContent(file.content(page)) { name ->
                fonts.getOrPut(name) { file.dict(fontDicts?.get(name))?.let { PdfFont.of(it, file) } ?: PdfFont.FALLBACK }
            }.run()
            lines(chunks)
        }.trim()
    }

    /** One run of text drawn at once: where it starts and ends on the page, its font size. */
    data class Chunk(val x: Double, val y: Double, val endX: Double, val size: Double, val text: String)

    fun lines(chunks: List<Chunk>): String {
        val sorted = chunks.filter { it.text.isNotBlank() }.sortedWith(compareByDescending<Chunk> { it.y }.thenBy { it.x })
        val rows = ArrayList<MutableList<Chunk>>()
        for (chunk in sorted) {
            val row = rows.lastOrNull()
            val lead = row?.first()
            if (lead != null && kotlin.math.abs(lead.y - chunk.y) < maxOf(lead.size, chunk.size) * SAME_LINE) row += chunk else rows += mutableListOf(chunk)
        }
        return rows.joinToString("\n") { row ->
            val line = StringBuilder()
            var end = Double.NaN
            for (chunk in row.sortedBy { it.x }) {
                if (!end.isNaN()) {
                    val gap = chunk.x - end
                    when {
                        gap > chunk.size * COLUMN_GAP -> line.append('\t')
                        gap > chunk.size * WORD_GAP && !line.endsWith(" ") && !chunk.text.startsWith(" ") -> line.append(' ')
                    }
                }
                line.append(chunk.text)
                end = maxOf(if (end.isNaN()) chunk.endX else end, chunk.endX)
            }
            line.toString().trim()
        }
    }

    /** Baselines closer than this share of the font size are one line. */
    private const val SAME_LINE = 0.5
    private const val WORD_GAP = 0.12
    private const val COLUMN_GAP = 1.6
}
