package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/** A word in a quick note that names the kind of account («карта», «efectivo»); the caller matches it to a real account. */
enum class AccountWord { CARD, CASH }

/**
 * One quick-note line read by rules (#586): [title] is what is left of the text, [daysAgo] is 0 for today, 1 for yesterday…
 * [currencyFound] is `false` when the line named no currency ([money] is in the fallback one).
 */
data class QuickNote(val money: Money, val currencyFound: Boolean, val title: String, val daysAgo: Int, val account: AccountWord?)

/**
 * Reads «кофе 3500», «вчера аптека 4200», «Субе по 30к», «cena 12500 ayer», «lunch 15,50 usd cash» without a model:
 * the amount ([AmountInText] plus «30к»/«5k» = thousands), the day (сегодня/вчера/позавчера, hoy/ayer/anteayer, today/yesterday),
 * the account word and the title. Code does all it can; a model is only for the lines this returns `null` for (no amount).
 */
object QuickNoteParser {
    private const val WORDS = "(?<![\\p{L}\\d])(%s)(?![\\p{L}\\d])"
    private val THOUSANDS = Regex("(?<![\\p{L}\\d.,])(\\d{1,3})(?:[.,](\\d{1,3}))?\\s?[кkКK](?![\\p{L}\\d])")
    private val DAYS = listOf(
        2 to listOf("позавчера", "anteayer", "antier"),
        1 to listOf("вчера", "ayer", "yesterday"),
        0 to listOf("сегодня", "hoy", "today"),
    ).map { (days, words) -> days to Regex(WORDS.format(words.joinToString("|")), RegexOption.IGNORE_CASE) }
    private val ACCOUNTS = listOf(
        AccountWord.CARD to Regex(
            WORDS.format("с карты|по карте|карта|карты|карте|картой|карту|карточка|карточкой|карточки|карточку|безнал|безналом|безналу|безналичные|безналичными|" +
                    "дебетовой|tarjeta|debito|débito|credito|crédito|card"),
            RegexOption.IGNORE_CASE,
        ),
        AccountWord.CASH to Regex(
            WORDS.format("наличные|наличными|наличкой|наличка|налички|наличку|налом|нала|налу|нал|наликами|наликом|налик|" +
                    "кэшем|кэша|кэш|кешем|кеша|кеш|кешом|кэшом|en efectivo|efectivo|al contado|contado|cash"),
            RegexOption.IGNORE_CASE,
        ),
    )
    /** A dangling preposition left at the end once the amount and the account word are gone («такси по», «кофе за», «обед с»). */
    private val FILLER = Regex("(^|\\s)(по|за|с|со|на|в|из|con|en|de|with|by)\\s*$", RegexOption.IGNORE_CASE)
    /** Words in front that say «I spent» and nothing about what (#638): «купил хлеб» → «хлеб», «pagué la luz» → «la luz». */
    private val LEADING = Regex(
        "^(купил|купила|купили|оплатил|оплатила|оплатили|заплатил|заплатила|заплатили|потратил|потратила|потратили|потрачено|" +
            "трата|расход|покупка|compré|compre|pagué|pague|gasté|gaste|bought|paid|spent)(\\s+(на|за|в|в магазине|en|por))?\\s+",
        RegexOption.IGNORE_CASE,
    )
    private const val EDGE = " \t-–—:·,;|"

    fun parse(
        line: String,
        fallback: CurrencyCode,
        known: Set<CurrencyCode> = emptySet(),
        dollarSign: CurrencyCode = CurrencyCode("ARS"),
    ): QuickNote? {
        val found = AmountInText.find(expandThousands(line), fallback, known, dollarSign) ?: return null
        var rest = found.note
        var daysAgo = 0
        for ((days, regex) in DAYS) if (regex.containsMatchIn(rest)) { daysAgo = days; rest = rest.replace(regex, " "); break }
        var account: AccountWord? = null
        for ((word, regex) in ACCOUNTS) if (regex.containsMatchIn(rest)) { account = word; rest = rest.replace(regex, " "); break }
        val title = cleanTitle(rest)
        return QuickNote(found.money, found.currencyFound, title, daysAgo, account)
    }

    /** Spaces squeezed, edges trimmed, «I spent» words in front and a dangling preposition at the end dropped. */
    internal fun cleanTitle(text: String): String {
        val squeezed = text.replace(Regex("\\s+"), " ").trim { it in EDGE }
        val noVerb = squeezed.replace(LEADING, "").ifBlank { squeezed }
        return noVerb.replace(FILLER, "").trim { it in EDGE }
    }

    /** «30к» → «30000», «1,5к» → «1500» (digits only, no floating point). */
    internal fun expandThousands(text: String): String = THOUSANDS.replace(text) { m ->
        val whole = m.groupValues[1]
        val fraction = m.groupValues[2].padEnd(3, '0')
        (whole + fraction).trimStart('0').ifEmpty { "0" }
    }
}
