package app.coshell.core.domain.categoryguess

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType

/**
 * Dictionary of popular brands and kinds of places («farmacia», «аптека», «Disco», «Uber»…) → the icon key of the starter
 * category they belong to (#584, owner idea 2026-11-21). Pure code: instant, 0 KB model, tested; finds the category of a
 * payee the family has never used, through the person's own category with that icon (survives renaming and languages).
 * A small model could not do this (61 % at best on local brands, `docs/research/on-device-ai.md`).
 */
object BrandDictionary {
    /** One-word names this short or this common must be the whole payee («Dia», «Personal»), or «Dia Market» would match a supermarket. */
    private val exactOnly = setOf("personal", "flow", "easy", "beat", "total", "delta", "gap", "max", "dia", "ok", "bp", "tnk", "wom", "epe", "abl", "spar", "bar", "chino", "amc", "star", "free now",
        "vital", "rodo", "nini", "dandy", "hogar", "feria", "ioma", "pami", "naldo", "small", "вита", "gulf", "ena", "ена", "kmg", "gpc", "санта", "корона", "super")

    private class Entry(val words: List<String>, val icon: String) {
        val exact = words.size == 1 && (words[0].length <= 3 || words[0] in exactOnly)
    }

    private val index: Map<String, List<Entry>> by lazy {
        val all = (listOf(ARGENTINA_BRANDS, ARGENTINA_MORE, ARGENTINA_EXTRA, RUSSIA_WORLD_BRANDS, CIS_BRANDS, EVERYDAY_WORDS)).flatMap { it.entries }
        buildMap<String, MutableList<Entry>> {
            for ((icon, names) in all) {
                for (name in names.split(',')) {
                    val words = wordsOf(name)
                    if (words.isNotEmpty()) getOrPut(words.first()) { mutableListOf() } += Entry(words, icon)
                }
            }
        }
    }

    /** Lower case, accents and «ё» folded, everything but letters and digits is a separator («MERPAGO*KIOSCO 123» → merpago kiosco 123). */
    fun wordsOf(text: String?): List<String> {
        if (text == null) return emptyList()
        val folded = buildString {
            for (c in text.lowercase()) append(fold(c))
        }
        return folded.map { if (it.isLetterOrDigit()) it else ' ' }.joinToString("").split(' ').filter { it.isNotEmpty() }
    }

    private fun fold(c: Char): Char = when (c) {
        'á', 'à', 'ä', 'â' -> 'a'
        'é', 'è', 'ë', 'ê' -> 'e'
        'í', 'ì', 'ï', 'î' -> 'i'
        'ó', 'ò', 'ö', 'ô' -> 'o'
        'ú', 'ù', 'ü', 'û' -> 'u'
        'ñ' -> 'n'
        'ё' -> 'е'
        else -> c
    }

    /** Icon key for [text] (payee, merchant, first line of a note); the longest match wins, `null` when nothing is known. */
    fun iconOf(text: String?): String? {
        val words = wordsOf(text)
        var best: Entry? = null
        for (i in words.indices) {
            for (e in index[words[i]].orEmpty()) {
                if (i + e.words.size > words.size || words.subList(i, i + e.words.size) != e.words) continue
                if (e.exact && words.size != 1 && !onlyWithNumbers(words, e)) continue
                if (best == null || e.words.size > best.words.size || (e.words.size == best.words.size && e.words.sumOf { it.length } > best.words.sumOf { it.length })) best = e
            }
        }
        return best?.icon ?: fuzzyIconOf(words)
    }

    /** One-word entries that may be matched loosely: long enough, not «exact only»; with their sound keys (#631). */
    private val loose: List<Pair<String, Entry>> by lazy {
        index.values.flatten().filter { it.words.size == 1 && !it.exact && it.words[0].length >= FUZZY_MIN && it.words[0].none(Char::isDigit) }
            .map { SoundKey.of(it.words[0]) to it }
    }

    /**
     * When nothing matched exactly: a word that sounds like a known one, one edit apart (two for long words) — «карфур» and «carrefor»
     * are Carrefour, «аптеке» is «аптека». The closest wins, then the longer one; words shorter than [FUZZY_MIN] are never guessed.
     */
    private fun fuzzyIconOf(words: List<String>): String? {
        var best: Entry? = null
        var bestDistance = Int.MAX_VALUE
        for (word in words) {
            if (word.length < FUZZY_MIN || word.any(Char::isDigit)) continue
            val key = SoundKey.of(word)
            val limit = if (key.length >= LONG_WORD) 2 else 1
            for ((entryKey, entry) in loose) {
                if (entryKey[0] != key[0]) continue
                val d = if (entryKey == key) 0 else SoundKey.distance(key, entryKey, limit)
                if (d > limit) continue
                if (d < bestDistance || (d == bestDistance && entry.words[0].length > (best?.words?.get(0)?.length ?: 0))) {
                    best = entry
                    bestDistance = d
                }
            }
        }
        return best?.icon
    }

    /** «Dia 123» is still the shop: the other words are digits only. */
    private fun onlyWithNumbers(words: List<String>, e: Entry) = words.all { it == e.words[0] || it.all(Char::isDigit) }

    /** The person's expense category with the [icon]; the first in their order, never an archived one. */
    fun categoryFor(icon: String?, categories: List<Category>): String? =
        if (icon == null) null else categories.filter { it.type == CategoryType.EXPENSE && !it.archived && it.icon == icon }.minByOrNull { it.sortOrder }?.id

    private const val FUZZY_MIN = 5
    private const val LONG_WORD = 8
}
