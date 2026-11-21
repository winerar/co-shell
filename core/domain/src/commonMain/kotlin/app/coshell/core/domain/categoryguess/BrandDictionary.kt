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
    private val exactOnly = setOf("personal", "flow", "easy", "beat", "total", "delta", "gap", "max", "dia", "ok", "bp", "tnk", "wom", "epe", "abl", "spar", "bar", "chino", "amc", "star", "free now")

    private class Entry(val words: List<String>, val icon: String) {
        val exact = words.size == 1 && (words[0].length <= 3 || words[0] in exactOnly)
    }

    private val index: Map<String, List<Entry>> by lazy {
        val all = (listOf(ARGENTINA_BRANDS, ARGENTINA_MORE, RUSSIA_WORLD_BRANDS)).flatMap { it.entries }
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
        return best?.icon
    }

    /** «Dia 123» is still the shop: the other words are digits only. */
    private fun onlyWithNumbers(words: List<String>, e: Entry) = words.all { it == e.words[0] || it.all(Char::isDigit) }

    /** The person's expense category with the [icon]; the first in their order, never an archived one. */
    fun categoryFor(icon: String?, categories: List<Category>): String? =
        if (icon == null) null else categories.filter { it.type == CategoryType.EXPENSE && !it.archived && it.icon == icon }.minByOrNull { it.sortOrder }?.id
}
