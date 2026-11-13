package app.coshell.core.model

/**
 * A coloured pill on operations («Испанский», «Аня», «отпуск»): one shared list for the family (#129).
 *
 * Operations keep tag references in [Transaction.labels]. A reference is resolved with [TagIds.of]: a tag
 * created in the app gets the id `TagIds.of(name)` when it is free, so labels imported from Wallet (plain
 * names) already point at their tags and two phones that create «Аня» offline make one tag, not two.
 * Renaming keeps the id. A deleted tag is a tombstone: operations keep the reference and simply show no pill.
 */
data class Tag(
    val id: String,
    val name: String,
    /** ARGB colour from the category palette; `null` — the screen picks one from [id]. */
    val color: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "Tag name must not be blank" }
        require(name.length <= MAX_NAME) { "Tag name is longer than $MAX_NAME" }
        require(id.isNotEmpty() && '/' !in id) { "Bad tag id: $id" }
    }

    companion object {
        const val MAX_NAME = 40
    }
}

object TagIds {
    /**
     * The id a reference (a tag name or a stored id) resolves to: trimmed, inner spaces collapsed, lower case,
     * `/` replaced (ids become server document keys). Random ids ([IdGenerator]) map to themselves.
     */
    fun of(reference: String): String =
        reference.trim().split(WHITESPACE).filter { it.isNotEmpty() }.joinToString(" ").lowercase().replace('/', '|')

    /** Tag ids of [labels], in order, without blanks and repeats. */
    fun resolve(labels: List<String>): List<String> = labels.map(::of).filter { it.isNotEmpty() }.distinct()

    /** `true` for a random id ([IdGenerator.Random], UUID): a reference that was never a name. */
    fun isGenerated(reference: String): Boolean = UUID.matches(reference)

    /** Display form of a name: trimmed, inner spaces collapsed, case kept. */
    fun cleanName(name: String): String = name.trim().split(WHITESPACE).filter { it.isNotEmpty() }.joinToString(" ")

    private val WHITESPACE = Regex("\\s+")
    private val UUID = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
}

/** Ids of the tags on this operation ([TagIds.resolve] of [Transaction.labels]). */
val Transaction.tagIds: List<String> get() = TagIds.resolve(labels)
