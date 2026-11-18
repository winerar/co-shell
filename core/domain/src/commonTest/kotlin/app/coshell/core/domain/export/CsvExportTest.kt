package app.coshell.core.domain.export

import app.coshell.core.domain.export.CsvExport.Column
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

class CsvExportTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val usdt = CurrencyCode("USDT")
    private val card = Account(id = "card", name = "Карта; USDT", currency = usdt)
    private val cash = Account(id = "cash", name = "Наличные", currency = usd)
    private val other = Account(id = "other", name = "Другой", currency = usd)

    private fun at(s: String) = Instant.parse(s)

    private val dinner = Transaction(
        id = "t1", type = TransactionType.EXPENSE, accountId = "card", amount = Money.ofMajor(13, 10, usdt),
        date = at("2026-11-13T21:40:00Z"), categoryId = "cafe", payee = "Café \"Martínez\"", note = "=SUM(A1)",
        labels = listOf("tag-anya", "gone"), originalAmount = Money.ofMajor(13_300, currency = ars), authorMemberId = "m1",
    )
    private val salary = Transaction(
        id = "t0", type = TransactionType.INCOME, accountId = "cash", amount = Money.ofMajor(1_234_567, 5, usd),
        date = at("2026-11-01T09:00:00Z"),
    )
    private val exchange = Transaction(
        id = "t2", type = TransactionType.TRANSFER, accountId = "cash", amount = Money.ofMajor(100, currency = usd),
        date = at("2026-11-20T12:00:00Z"), toAccountId = "card", toAmount = Money.ofMajor(99, 50, usdt),
    )
    private val elsewhere = Transaction(
        id = "t3", type = TransactionType.EXPENSE, accountId = "other", amount = Money.ofMajor(5, currency = usd),
        date = at("2026-11-05T10:00:00Z"),
    )
    private val december = salary.copy(id = "t4", date = at("2026-12-01T00:00:00Z"))
    private val all = listOf(dinner, salary, exchange, elsewhere, december)

    private val november = CsvExport.Filter(at("2026-11-01T00:00:00Z"), at("2026-12-01T00:00:00Z"))

    private val names = CsvExport.Names(
        accounts = listOf(card, cash, other).associateBy(Account::id),
        category = { if (it == "cafe") "Еда · Кафе" else null },
        tag = { if (it == "tag-anya") "Аня" else "" },
        member = { if (it == "m1") "Маша" else null },
        date = { it.toString().take(16).replace('T', ' ') },
    )
    private val labels = CsvExport.Labels(
        columns = Column.entries.associateWith { it.name.lowercase() },
        types = mapOf(TransactionType.EXPENSE to "Расход", TransactionType.INCOME to "Доход", TransactionType.TRANSFER to "Перевод"),
    )

    private fun rows(csv: String) = csv.removePrefix("﻿").removeSuffix("\r\n").split("\r\n")

    @Test
    fun `header and rows are in date order with bom and crlf`() {
        val csv = CsvExport.build(all, november, names, labels)
        assertTrue(csv.startsWith("﻿date;type;account;amount;"))
        val lines = rows(csv)
        assertEquals(5, lines.size) // header + salary, elsewhere, dinner, exchange
        assertTrue(lines[1].startsWith("2026-11-01 09:00;Доход;"))
        assertTrue(lines[4].startsWith("2026-11-20 12:00;Перевод;"))
    }

    @Test
    fun `income is positive with a comma and no grouping`() {
        val line = rows(CsvExport.build(listOf(salary), november, names, labels))[1]
        assertEquals("2026-11-01 09:00;Доход;Наличные;1234567,05;USD;;;;;;;;;;", line)
    }

    @Test
    fun `expense is negative, receipt and author filled, text quoted and guarded`() {
        val line = rows(CsvExport.build(listOf(dinner), november, names, labels))[1]
        assertEquals(
            "2026-11-13 21:40;Расход;\"Карта; USDT\";-13,10;USDT;Еда · Кафе;\"Café \"\"Martínez\"\"\";'=SUM(A1);Аня;13300,00;ARS;;;;Маша",
            line,
        )
    }

    @Test
    fun `transfer shows both sides`() {
        val line = rows(CsvExport.build(listOf(exchange), november, names, labels))[1]
        assertEquals("2026-11-20 12:00;Перевод;Наличные;-100,00;USD;;;;;;;\"Карта; USDT\";99,50;USDT;", line)
    }

    @Test
    fun `account filter keeps transfers touching a picked account`() {
        val onlyCard = november.copy(accountIds = setOf("card"))
        assertEquals(listOf("t1", "t2"), CsvExport.select(all, onlyCard).map(Transaction::id))
        assertEquals(listOf("t3"), CsvExport.select(all, november.copy(accountIds = setOf("other"))).map(Transaction::id))
    }

    @Test
    fun `period end is exclusive`() {
        assertEquals(listOf("t0", "t3", "t1", "t2"), CsvExport.select(all, november).map(Transaction::id))
    }

    @Test
    fun `quoting rules`() {
        assertEquals("plain", CsvExport.quote("plain"))
        assertEquals("\"a\nb\"", CsvExport.quote("a\nb"))
        assertEquals("\" pad\"", CsvExport.quote(" pad"))
        assertEquals("'-скидка", CsvExport.text("-скидка"))
        assertEquals("", CsvExport.text(""))
    }

    @Test
    fun `invalid filters and labels are rejected`() {
        assertFailsWith<IllegalArgumentException> { CsvExport.Filter(november.until, november.from) }
        assertFailsWith<IllegalArgumentException> { november.copy(accountIds = emptySet()) }
        assertFailsWith<IllegalArgumentException> { labels.copy(columns = mapOf(Column.DATE to "d")) }
    }
}
