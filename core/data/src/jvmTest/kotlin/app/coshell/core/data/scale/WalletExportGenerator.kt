package app.coshell.core.data.scale

import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * A synthetic Wallet export for scale tests: five accounts in four currencies, ~20 categories,
 * transfer pairs, notes, payees and labels over about four years. Deterministic. The same mix as
 * `scripts/agent/gen-wallet-export.py`, which makes a file to import on a phone.
 */
object WalletExportGenerator {
    private const val HEADER = "account;category;currency;amount;ref_currency_amount;type;note;date;transfer;payee;labels"
    private val accounts = listOf("Наличные ARS" to "ARS", "Карта USD" to "USD", "USDT" to "USD", "Карта RUB" to "RUB", "Счёт AMD" to "AMD")
    private val expenses = listOf("Продукты", "Рестораны", "Кафе", "Транспорт", "Такси", "Дети", "Образование", "Здоровье",
        "Аптека", "Одежда", "Дом", "Связь", "Подписки", "Подарки", "Развлечения", "Путешествия")
    private val incomes = listOf("Зарплата", "Проценты с вкладов, дивиденды", "Возврат")
    private val notes = listOf("", "", "", "Овощи", "Обед", "Коляска: аванс", "\"Аренда; ноябрь\"")
    private val payees = listOf("", "", "", "Coto", "Carrefour", "Uber", "Netflix")
    private val labels = listOf("", "", "", "", "Кофе", "Пекарня", "Отпуск")

    fun generate(rows: Int, seed: Int = 131): String {
        val random = Random(seed)
        var minute = 4L * 365 * 24 * 60 // count down from about four years after the epoch below
        val step = minute / rows
        val lines = ArrayList<String>(rows + 1).apply { add(HEADER) }
        fun amount() = "${random.nextInt(1, 200_000)}.${random.nextInt(10, 99)}"
        fun line(acc: Pair<String, String>, category: String, sign: String, transfer: Boolean, extra: Boolean, abs: String = amount()): String {
            val value = sign + abs // Wallet pairs transfer rows by equal |ref_currency_amount|
            val note = if (extra) notes.random(random) else ""
            val payee = if (extra) payees.random(random) else ""
            val label = if (extra) labels.random(random) else ""
            return "${acc.first};$category;${acc.second};$value;$value;x;$note;${date(minute)};$transfer;$payee;$label"
        }
        while (lines.size <= rows) {
            minute -= random.nextLong(step / 2, step * 3 / 2 + 1)
            val dice = random.nextDouble()
            when {
                dice < 0.06 && rows - lines.size >= 1 -> {
                    val (from, to) = accounts.shuffled(random).take(2)
                    val abs = amount()
                    lines += line(from, "TRANSFER", "-", transfer = true, extra = false, abs = abs)
                    lines += line(to, "TRANSFER", "", transfer = true, extra = false, abs = abs)
                }
                dice < 0.12 -> lines += line(accounts.random(random), incomes.random(random), "", transfer = false, extra = false)
                else -> lines += line(accounts.random(random), expenses.random(random), "-", transfer = false, extra = true)
            }
        }
        return lines.joinToString("\n")
    }

    private val start = LocalDateTime(2022, 11, 1, 0, 0).toInstant(TimeZone.UTC)

    /** Minutes after 2022-11-01 00:00 → "yyyy-MM-dd HH:mm:ss". */
    private fun date(minutes: Long): String = (start + minutes.minutes).toLocalDateTime(TimeZone.UTC)
        .run { "%04d-%02d-%02d %02d:%02d:00".format(year, month.number, day, hour, minute) }
}
