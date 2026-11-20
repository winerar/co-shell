package app.coshell.receipt.document

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * The objects of a PDF found by scanning for `N G obj … endobj` (no xref needed — damaged files read too), plus the
 * objects packed in object streams. Only FlateDecode streams are decoded; encrypted files give no pages.
 * Pages come from the page tree (`/Root → /Pages → /Kids`), with inherited `/Resources`.
 */
internal class PdfFile private constructor(private val objects: Map<Int, PdfObject>, private val trailer: PdfDict?) {

    fun resolve(value: PdfValue?): PdfValue? = if (value is PdfRef) objects[value.number]?.value else value

    fun obj(value: PdfValue?): PdfObject? = (value as? PdfRef)?.let { objects[it.number] }

    fun dict(value: PdfValue?): PdfDict? = resolve(value) as? PdfDict

    /** Each page's dictionary and its resources, in reading order; at most [limit]. */
    fun pages(limit: Int): List<Pair<PdfDict, PdfDict?>> {
        if (trailer?.get("Encrypt") != null) return emptyList()
        val root = dict(trailer?.get("Root")) ?: objects.values.mapNotNull { it.dict }.firstOrNull { it.type == "Catalog" } ?: return emptyList()
        val out = ArrayList<Pair<PdfDict, PdfDict?>>()
        fun walk(node: PdfDict?, inherited: PdfDict?, depth: Int) {
            if (node == null || out.size >= limit || depth > MAX_DEPTH) return
            val resources = dict(node["Resources"]) ?: inherited
            if (node.type == "Page" || node["Kids"] == null) {
                out += node to resources
            } else {
                (resolve(node["Kids"]) as? PdfArray)?.items?.forEach { walk(dict(it), resources, depth + 1) }
            }
        }
        walk(dict(root["Pages"]), null, 0)
        return out
    }

    /** The decoded content of a page: one stream or an array of them, joined. */
    fun content(page: PdfDict): ByteArray {
        val parts = when (val contents = page["Contents"]) {
            is PdfArray -> contents.items
            is PdfRef -> (objects[contents.number]?.value as? PdfArray)?.items ?: listOf(contents)
            else -> emptyList()
        }
        val out = ByteArrayOutputStream()
        parts.forEach { ref -> obj(ref)?.stream?.let { out.write(it); out.write('\n'.code) } }
        return out.toByteArray()
    }

    companion object {
        private const val MAX_DEPTH = 16
        private val HEADER = Regex("""(\d{1,7})\s+\d{1,5}\s+obj\b""")

        fun parse(data: ByteArray): PdfFile {
            val text = String(data, Charsets.ISO_8859_1)
            val objects = HashMap<Int, PdfObject>()
            var trailer: PdfDict? = null
            // Binary stream data can look like «12 0 obj»: matches inside the last stream read are skipped.
            var consumed = 0
            for (match in HEADER.findAll(text)) {
                if (match.range.first < consumed) continue
                val number = match.groupValues[1].toInt()
                val lexer = PdfLexer(data, match.range.last + 1)
                val value = lexer.next() ?: continue
                val dict = value as? PdfDict
                val stream = if (dict != null) streamAfter(data, text, lexer, dict) else null
                consumed = stream?.second ?: lexer.pos
                objects[number] = PdfObject(value, stream?.first)
                if (dict?.type == "XRef") trailer = dict
            }
            text.lastIndexOf("trailer").takeIf { it >= 0 }?.let { at ->
                (PdfLexer(data, at + "trailer".length).next() as? PdfDict)?.let { trailer = it }
            }
            objects.values.filter { it.dict?.type == "ObjStm" }.forEach { unpack(it, objects) }
            return PdfFile(objects, trailer)
        }

        /** The decoded stream (`null` — unknown filter) and where its raw data ends; `null` — the object has no stream. */
        private fun streamAfter(data: ByteArray, text: String, lexer: PdfLexer, dict: PdfDict): Pair<ByteArray?, Int>? {
            lexer.skipSpace()
            if (!text.startsWith("stream", lexer.pos)) return null
            var start = lexer.pos + "stream".length
            if (text.getOrNull(start) == '\r') start++
            if (text.getOrNull(start) == '\n') start++
            val length = (dict["Length"] as? PdfNumber)?.int
            val end = if (length != null && start + length <= data.size && text.startsWith("endstream", skipEol(text, start + length))) {
                start + length
            } else {
                text.indexOf("endstream", start).takeIf { it >= 0 } ?: return null
            }
            return decode(data.copyOfRange(start, end), dict) to end
        }

        private fun skipEol(text: String, at: Int): Int {
            var i = at
            while (i < text.length && text[i].isWhitespace()) i++
            return i
        }

        private fun decode(raw: ByteArray, dict: PdfDict): ByteArray? {
            val filters = when (val f = dict["Filter"]) {
                null -> emptyList()
                is PdfName -> listOf(f.name)
                is PdfArray -> f.items.mapNotNull { (it as? PdfName)?.name }
                else -> return null
            }
            return filters.fold(raw as ByteArray?) { bytes, filter ->
                if (bytes == null) null else if (filter == "FlateDecode" || filter == "Fl") inflate(bytes) else null
            }
        }

        private fun inflate(bytes: ByteArray): ByteArray? {
            val inflater = Inflater()
            return try {
                inflater.setInput(bytes)
                val out = ByteArrayOutputStream(bytes.size * 4)
                val buffer = ByteArray(8192)
                while (!inflater.finished()) {
                    val n = inflater.inflate(buffer)
                    if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                    out.write(buffer, 0, n)
                    if (out.size() > MAX_STREAM) return null
                }
                out.toByteArray()
            } catch (_: DataFormatException) {
                null
            } finally {
                inflater.end()
            }
        }

        /** Objects packed in a `/Type /ObjStm`: `N` pairs «number offset», then the values from `/First`. */
        private fun unpack(stream: PdfObject, into: MutableMap<Int, PdfObject>) {
            val data = stream.stream ?: return
            val dict = stream.dict ?: return
            val count = (dict["N"] as? PdfNumber)?.int ?: return
            val first = (dict["First"] as? PdfNumber)?.int ?: return
            val header = PdfLexer(data, 0, minOf(first, data.size))
            repeat(count) {
                val number = (header.next() as? PdfNumber)?.int ?: return
                val offset = (header.next() as? PdfNumber)?.int ?: return
                if (first + offset >= data.size) return@repeat
                val value = PdfLexer(data, first + offset).next() ?: return@repeat
                into.putIfAbsent(number, PdfObject(value))
            }
        }

        private const val MAX_STREAM = 8 * 1024 * 1024
    }
}

internal val PdfDict.type: String? get() = (this["Type"] as? PdfName)?.name
