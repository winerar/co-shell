package app.coshell.transactions.edit

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.tags.SaveTagUseCase
import app.coshell.core.domain.tags.TagRanking
import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Tag
import app.coshell.core.model.TagIds
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Tags for the entry sheet (#293, boards `Fb129A`, `Fb129Pick`): the live list with usage and creating a tag.
 * Keeps [TransactionEditViewModel] thin, like [EntryTemplates].
 */
class EntryTags @Inject constructor(
    private val tags: TagRepository,
    private val saveTag: SaveTagUseCase,
) {
    fun observe(): Flow<TagChoices> = combine(tags.observeTags(), tags.observeUsage(), ::tagChoices)

    /**
     * Creates a tag and returns its id. A name that is already taken (case and spaces ignored) returns the
     * existing tag — typing «аня» when «Аня» exists simply picks «Аня». `null` — a blank or too long name.
     */
    suspend fun create(name: String, color: Long?): String? = when (val result = saveTag(null, name, color)) {
        is SaveTagUseCase.Result.Saved -> result.tag.id
        is SaveTagUseCase.Result.Invalid -> when (result.reason) {
            SaveTagUseCase.Reason.DUPLICATE_NAME -> {
                val key = TagIds.of(name)
                tags.getTags().firstOrNull { TagIds.of(it.name) == key }?.id
            }
            else -> null
        }
    }
}

/** Every live tag by name ([options]) and the ones used last ([recentIds], newest first). */
data class TagChoices(
    val options: List<TagOption> = emptyList(),
    val recentIds: List<String> = emptyList(),
)

/** A tag as the pickers show it: [color] — a stored palette value, [count] — live operations with it. */
data class TagOption(val id: String, val name: String, val color: Long, val count: Int = 0)

internal fun tagChoices(tags: List<Tag>, usage: Map<String, TagUsage>) = TagChoices(
    options = tags.sortedBy { it.name.lowercase() }
        .map { TagOption(it.id, it.name, it.color ?: CategoryPalette.forKey(it.id), usage[it.id]?.count ?: 0) },
    recentIds = TagRanking.recent(tags, usage, RECENT_TAGS).map { it.id },
)

/**
 * Labels to save: [original] references that stay chosen keep their form (a Wallet label «Mercado Libre» is not
 * rewritten to its id), newly chosen tags are added by id. A reference to a deleted tag stays chosen because the
 * form starts with every resolved id of the operation — it is never shown, so never dropped.
 */
internal fun mergeLabels(original: List<String>, selected: List<String>): List<String> {
    val kept = original.filter { TagIds.of(it) in selected }.distinctBy(TagIds::of)
    val keptIds = kept.map(TagIds::of).toSet()
    return kept + selected.filter { it !in keptIds }
}

/** Tags whose name holds [query] — names starting with it first ([TagRanking.search]); blank — all. */
internal fun searchTags(options: List<TagOption>, query: String): List<TagOption> {
    if (query.isBlank()) return options
    val byId = options.associateBy { it.id }
    return TagRanking.search(options.map { Tag(it.id, it.name) }, query).mapNotNull { byId[it.id] }
}

/** How many recent tags are known; the «Теги» sheet shows the first [RECENT_IN_SHEET] of them. */
internal const val RECENT_TAGS = 8
internal const val RECENT_IN_SHEET = 4
