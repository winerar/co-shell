package app.coshell.core.domain.categoryguess

/**
 * A rough «how it sounds» key of a folded word for the fuzzy step of [BrandDictionary] (#631): Cyrillic is written in Latin
 * letters and spellings that sound alike meet — «карфур», «carrefour» and «carrefor» become «karfur», «karefur», «karefor»,
 * one edit apart. Pure code, no tables bigger than the alphabet.
 */
internal object SoundKey {
    private val CYRILLIC = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ж' to "j", 'з' to "z", 'и' to "i", 'й' to "i",
        'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "sh", 'ъ' to "", 'ы' to "i", 'ь' to "", 'э' to "e",
        'ю' to "iu", 'я' to "ia", 'і' to "i", 'ї' to "i", 'є' to "e", 'ґ' to "g",
    )
    private val SOUNDS = listOf(
        "sch" to "sh", "ph" to "f", "th" to "t", "ou" to "u", "oo" to "u", "ee" to "i", "ck" to "k", "qu" to "k", "kh" to "h",
        "c" to "k", "q" to "k", "w" to "v", "y" to "i", "x" to "ks", "z" to "s",
    )

    /** The key of one folded word (see [BrandDictionary.wordsOf]); digits stay as they are. */
    fun of(word: String): String {
        val latin = buildString { for (c in word) append(CYRILLIC[c] ?: c.toString()) }
        var key = latin
        for ((from, to) in SOUNDS) key = key.replace(from, to)
        // Doubled letters sound as one («carrefour» → «karefur»).
        return buildString {
            for (c in key) if (isEmpty() || last() != c) append(c)
        }
    }

    /** Edit distance with swaps of two neighbours (optimal string alignment), stopping early once it passes [limit]. */
    fun distance(a: String, b: String, limit: Int): Int {
        if (kotlin.math.abs(a.length - b.length) > limit) return limit + 1
        var prev2 = IntArray(b.length + 1)
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val row = IntArray(b.length + 1)
            row[0] = i
            var best = row[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var v = minOf(prev[j] + 1, row[j - 1] + 1, prev[j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) v = minOf(v, prev2[j - 2] + 1)
                row[j] = v
                if (v < best) best = v
            }
            if (best > limit) return limit + 1
            prev2 = prev
            prev = row
        }
        return prev[b.length]
    }
}
