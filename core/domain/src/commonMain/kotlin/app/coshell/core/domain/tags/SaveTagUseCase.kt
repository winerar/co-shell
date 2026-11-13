package app.coshell.core.domain.tags

import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Tag
import app.coshell.core.model.TagIds

/**
 * Creates or edits a tag. The name is cleaned ([TagIds.cleanName]) and must be unique among live tags
 * ignoring case and spaces. A new tag gets the id `TagIds.of(name)` when no row has it — so operations
 * imported with that label show it — otherwise a random one. Editing keeps the id.
 */
class SaveTagUseCase(
    private val tags: TagRepository,
    private val ids: IdGenerator,
) {
    sealed interface Result {
        data class Saved(val tag: Tag) : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason { BLANK_NAME, NAME_TOO_LONG, DUPLICATE_NAME, NOT_FOUND }

    /** [id] `null` — a new tag. */
    suspend operator fun invoke(id: String?, name: String, color: Long?): Result {
        val clean = TagIds.cleanName(name)
        if (clean.isEmpty()) return Result.Invalid(Reason.BLANK_NAME)
        if (clean.length > Tag.MAX_NAME) return Result.Invalid(Reason.NAME_TOO_LONG)
        if (id != null && tags.getTag(id) == null) return Result.Invalid(Reason.NOT_FOUND)
        val key = TagIds.of(clean)
        if (tags.getTags().any { it.id != id && TagIds.of(it.name) == key }) return Result.Invalid(Reason.DUPLICATE_NAME)
        val tagId = id ?: key.takeUnless { tags.isIdTaken(it) } ?: ids.newId()
        val tag = Tag(tagId, clean, color)
        tags.saveTag(tag)
        return Result.Saved(tag)
    }
}
