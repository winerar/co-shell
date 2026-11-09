package app.coshell.core.domain.importer.csv

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvReaderTest {

    private fun rows(text: String): List<CsvRow> =
        (CsvReader.read(text) as CsvResult.Success).rows

    @Test
    fun splitsBySemicolonAndKeepsEmptyFields() {
        assertEquals(
            listOf(CsvRow(1, listOf("a", "", "c", "")), CsvRow(2, listOf("d", "e"))),
            rows("a;;c;\nd;e\n"),
        )
    }

    @Test
    fun stripsBomAndHandlesCrLfAndMissingFinalNewline() {
        assertEquals(
            listOf(CsvRow(1, listOf("h1", "h2")), CsvRow(2, listOf("1", "2"))),
            rows("﻿h1;h2\r\n1;2"),
        )
    }

    @Test
    fun skipsBlankLinesButKeepsLineNumbers() {
        assertEquals(
            listOf(CsvRow(1, listOf("a")), CsvRow(3, listOf("b"))),
            rows("a\n\nb\n\n"),
        )
    }

    @Test
    fun quotedFieldMayHoldDelimiterQuotesAndLineBreaks() {
        val result = rows("note;x\n\"Кофе; булка\n\"\"вкусно\"\"\";1\nnext;2\n")
        assertEquals(CsvRow(2, listOf("Кофе; булка\n\"вкусно\"", "1")), result[1])
        assertEquals(CsvRow(4, listOf("next", "2")), result[2])
    }

    @Test
    fun emptyQuotedFieldIsEmpty() {
        assertEquals(listOf(CsvRow(1, listOf("", "a"))), rows("\"\";a"))
    }

    @Test
    fun strayQuoteInsideUnquotedFieldIsText() {
        assertEquals(listOf(CsvRow(1, listOf("5\" screen", "b"))), rows("5\" screen;b"))
    }

    @Test
    fun unterminatedQuoteReportsItsLine() {
        assertEquals(CsvResult.UnterminatedQuote(2), CsvReader.read("a;b\n\"open;c\nmore\n"))
    }

    @Test
    fun emptyTextHasNoRows() {
        assertEquals(emptyList(), rows(""))
        assertEquals(emptyList(), rows("﻿"))
    }
}
