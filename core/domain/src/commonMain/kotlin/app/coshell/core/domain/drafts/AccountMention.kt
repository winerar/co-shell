package app.coshell.core.domain.drafts

import app.coshell.core.domain.categoryguess.BrandDictionary
import app.coshell.core.domain.categoryguess.SoundKey
import app.coshell.core.model.Account

/**
 * An account named in a quick-note line by a word of its name (#632): «такси 2000 сбер» → «Сбер. счёт AMD», «кофе 3500 тинькофф» →
 * «Tinkoff» (the same sound in another alphabet), «обед 12к мп» → «Mercado Pago» (initials of a two-word name). Words every account
 * name may have («счёт», «карта», a currency code) do not count. [title] is the line's title without that word; a word that is the
 * whole title is the shop, not an account.
 */
internal data class AccountMention(val account: Account, val title: String) {
    companion object {
        private const val MIN_WORD = 3
        private const val MIN_PREFIX = 4
        private val GENERIC = setOf(
            "счет", "счета", "карта", "card", "cuenta", "tarjeta", "cash", "наличные", "efectivo", "банк", "bank", "banco", "main", "основной",
            "мой", "моя", "my", "mi", "кошелек", "wallet", "billetera", "debit", "credit", "дебет", "кредит", "общий", "личный", "семья",
        )
        /** Two-letter words of a sentence, never initials. */
        private val SHORT_WORDS = setOf("на", "за", "по", "из", "со", "до", "от", "не", "ну", "en", "de", "la", "el", "lo", "me", "mi", "to", "at", "in", "on", "by", "of")

        /** The account [title] names, `null` when none or when two accounts fit the word equally. */
        fun find(title: String, accounts: List<Account>): AccountMention? {
            val tokens = title.split(' ').filter { it.isNotBlank() }
            for (token in tokens) {
                val word = BrandDictionary.wordsOf(token).singleOrNull() ?: continue
                if (word.length < 2 || word in GENERIC || word in SHORT_WORDS || word.any(Char::isDigit)) continue
                val fits = accounts.filter { names(it, word) }
                val account = fits.singleOrNull() ?: continue
                val rest = tokens.filter { it !== token }.joinToString(" ")
                // «Carrefour 5000» with a «Carrefour» card is the shop, not the account: a mention never takes the whole title.
                if (rest.isBlank()) continue
                return AccountMention(account, rest)
            }
            return null
        }

        private fun names(account: Account, word: String): Boolean {
            val nameWords = BrandDictionary.wordsOf(account.name).filter { it !in GENERIC && it.lowercase() != account.currency.value.lowercase() }
            if (word.length == 2) {
                // «мп» / «mp» — the initials of a name of two or more words, in either alphabet.
                val initials = nameWords.filter { w -> w.none(Char::isDigit) }.joinToString("") { it.take(1) }
                return nameWords.size >= 2 && SoundKey.of(initials) == SoundKey.of(word)
            }
            if (word.length < MIN_WORD) return false
            val key = SoundKey.of(word)
            return nameWords.any { w ->
                val k = SoundKey.of(w)
                w == word || k == key || (word.length >= MIN_PREFIX && (w.startsWith(word) || k.startsWith(key)))
            }
        }
    }
}
