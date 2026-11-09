package app.coshell.core.domain.importer.csv

/** One parsed CSV record. [line] is the 1-based line where the record starts in the file. */
data class CsvRow(val line: Int, val fields: List<String>)

sealed interface CsvResult {
    data class Success(val rows: List<CsvRow>) : CsvResult

    /** A quoted field opened on [line] is never closed: the rest of the file cannot be trusted. */
    data class UnterminatedQuote(val line: Int) : CsvResult
}

/**
 * Minimal RFC 4180 reader (pure Kotlin, no `java.*`): [delimiter]-separated fields, `"` quotes,
 * `""` inside quotes is a literal quote, line breaks inside quotes belong to the field.
 *
 * Also tolerated: a UTF-8 BOM, `\r\n` / `\r` / `\n` line ends, blank lines (skipped), a missing
 * final line break. Fields are returned as written — trimming is the caller's choice.
 */
object CsvReader {

    private const val BOM = '﻿'
    private const val QUOTE = '"'

    fun read(text: String, delimiter: Char = ';'): CsvResult {
        val rows = mutableListOf<CsvRow>()
        val fields = mutableListOf<String>()
        val field = StringBuilder()
        var line = 1
        var rowStart = 1
        var inQuotes = false
        var quoteStart = 0
        var fieldStarted = false
        var i = if (text.startsWith(BOM)) 1 else 0

        fun endField() {
            fields += field.toString()
            field.clear()
            fieldStarted = false
        }

        fun endRow() {
            endField()
            val blank = fields.size == 1 && fields[0].isEmpty()
            if (!blank) rows += CsvRow(rowStart, fields.toList())
            fields.clear()
        }

        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                when {
                    c == QUOTE && text.getOrNull(i + 1) == QUOTE -> { field.append(QUOTE); i++ }
                    c == QUOTE -> inQuotes = false
                    else -> {
                        if (c == '\n' || (c == '\r' && text.getOrNull(i + 1) != '\n')) line++
                        field.append(c)
                    }
                }
            } else {
                when (c) {
                    QUOTE -> if (!fieldStarted && field.isEmpty()) {
                        inQuotes = true
                        quoteStart = line
                        fieldStarted = true
                    } else {
                        field.append(c) // a stray quote in an unquoted field is kept as text
                    }
                    delimiter -> endField()
                    '\r', '\n' -> {
                        if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
                        endRow()
                        line++
                        rowStart = line
                    }
                    else -> { field.append(c); fieldStarted = true }
                }
            }
            i++
        }
        if (inQuotes) return CsvResult.UnterminatedQuote(quoteStart)
        if (field.isNotEmpty() || fields.isNotEmpty() || fieldStarted) endRow()
        return CsvResult.Success(rows)
    }
}
