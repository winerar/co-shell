package app.coshell.core.domain.export

import app.coshell.core.model.Account
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.time.Instant

/**
 * Operations as a CSV file for spreadsheets and the tax office (#204, owner 2026-11-17: «Ещё → Экспорт», variant А).
 *
 * Format for Excel and Google Sheets in ru/es locales: `;` between fields, a comma in amounts, no digit grouping,
 * UTF-8 with a BOM (Excel guesses the encoding by it), CRLF line ends. Amounts are signed by their effect on the
 * account: an expense and the outgoing side of a transfer are negative. Rows go by date, oldest first.
 *
 * Every human text (column names, types, dates) comes from the caller: the domain has no strings (golden rule 9).
 */
object CsvExport {

    const val SEPARATOR = ';'
    private const val BOM = "﻿"
    private const val EOL = "\r\n"
    private val number = MoneyFormat(decimalSeparator = ',', groupSeparator = ' ')

    enum class Column {
        DATE, TYPE, ACCOUNT, AMOUNT, CURRENCY, CATEGORY, PAYEE, NOTE, TAGS,
        RECEIPT_AMOUNT, RECEIPT_CURRENCY, TO_ACCOUNT, TO_AMOUNT, TO_CURRENCY, AUTHOR,
    }

    /** Texts from resources: one name per [Column] and one per [TransactionType]. */
    data class Labels(val columns: Map<Column, String>, val types: Map<TransactionType, String>) {
        init {
            require(columns.keys == Column.entries.toSet()) { "Every column needs a name" }
            require(types.keys == TransactionType.entries.toSet()) { "Every type needs a name" }
        }
    }

    /** What to export: [from] inclusive, [until] exclusive; [accountIds] `null` — every account. */
    data class Filter(val from: Instant, val until: Instant, val accountIds: Set<String>? = null) {
        init {
            require(from < until) { "Empty period" }
            require(accountIds == null || accountIds.isNotEmpty()) { "Pick at least one account" }
        }

        fun matches(t: Transaction): Boolean {
            if (t.date < from || t.date >= until) return false
            val ids = accountIds ?: return true
            return t.accountId in ids || t.toAccountId in ids
        }
    }

    /** How names are looked up; all take ids stored in [Transaction]. */
    data class Names(
        val accounts: Map<String, Account>,
        /** «Еда · Кафе» for a subcategory; `null` — unknown or no category. */
        val category: (String) -> String?,
        /** A tag reference ([Transaction.labels]) to its name. */
        val tag: (String) -> String,
        val member: (String) -> String?,
        /** Local date and time as the person reads it, e.g. «2026-11-17 21:40». */
        val date: (Instant) -> String,
    )

    /** Rows that pass [filter] (handy for «операций: N» before the file is built). */
    fun select(transactions: List<Transaction>, filter: Filter): List<Transaction> =
        transactions.filter(filter::matches).sortedWith(compareBy(Transaction::date, Transaction::id))

    fun build(transactions: List<Transaction>, filter: Filter, names: Names, labels: Labels): String {
        val out = StringBuilder(BOM)
        line(out, Column.entries.map { labels.columns.getValue(it) })
        for (t in select(transactions, filter)) line(out, row(t, names, labels).let { r -> Column.entries.map { r[it].orEmpty() } })
        return out.toString()
    }

    private fun row(t: Transaction, names: Names, labels: Labels): Map<Column, String> = buildMap {
        put(Column.DATE, names.date(t.date))
        put(Column.TYPE, labels.types.getValue(t.type))
        put(Column.ACCOUNT, text(names.accounts[t.accountId]?.name.orEmpty()))
        put(Column.AMOUNT, amount(if (t.type == TransactionType.INCOME) t.amount else -t.amount))
        put(Column.CURRENCY, t.amount.currency.value)
        t.categoryId?.let(names.category)?.let { put(Column.CATEGORY, text(it)) }
        t.payee?.let { put(Column.PAYEE, text(it)) }
        put(Column.NOTE, text(t.note))
        put(Column.TAGS, text(t.labels.map(names.tag).filter(String::isNotBlank).joinToString(", ")))
        t.originalAmount?.let {
            put(Column.RECEIPT_AMOUNT, amount(it))
            put(Column.RECEIPT_CURRENCY, it.currency.value)
        }
        if (t.type == TransactionType.TRANSFER) {
            put(Column.TO_ACCOUNT, text(t.toAccountId?.let(names.accounts::get)?.name.orEmpty()))
            t.toAmount?.let {
                put(Column.TO_AMOUNT, amount(it))
                put(Column.TO_CURRENCY, it.currency.value)
            }
        }
        t.authorMemberId?.let(names.member)?.let { put(Column.AUTHOR, text(it)) }
    }

    private fun amount(money: Money): String =
        number.formatNumber(money.amountMinor, Currencies.info(money.currency).fractionDigits).replace(" ", "")

    /**
     * Free text from people: a leading `=`, `+`, `-` or `@` would run as a formula in a spreadsheet, so it gets an
     * apostrophe (the usual guard; Excel hides it).
     */
    internal fun text(value: String): String =
        if (value.isNotEmpty() && value[0] in "=+-@\t\r") "'$value" else value

    private fun line(out: StringBuilder, fields: List<String>) {
        fields.joinTo(out, SEPARATOR.toString(), transform = ::quote)
        out.append(EOL)
    }

    internal fun quote(field: String): String {
        val needs = field.any { it == SEPARATOR || it == '"' || it == '\n' || it == '\r' } ||
            field.startsWith(' ') || field.endsWith(' ')
        return if (needs) "\"" + field.replace("\"", "\"\"") + "\"" else field
    }
}
