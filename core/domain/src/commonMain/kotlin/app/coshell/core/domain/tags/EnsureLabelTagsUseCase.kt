package app.coshell.core.domain.tags

import app.coshell.core.model.Tag
import app.coshell.core.model.TagIds
import kotlinx.coroutines.flow.first

/**
 * Turns labels imported from Wallet into tags (#129): every reference used by a live operation that has no
 * tag row gets a tag named as the label was written, without a colour (the screen picks one). A deleted
 * tag is not brought back, and a random id (a tag from the other phone that has not arrived yet) is no label. Runs at start-up and after an import; returns how many tags were added.
 */
class EnsureLabelTagsUseCase(private val tags: TagRepository) {
    suspend operator fun invoke(): Int {
        val missing = tags.observeUsage().first().filter { (id, use) ->
            !TagIds.isGenerated(id) && '/' !in id && !tags.isIdTaken(id) && TagIds.cleanName(use.label).length in 1..Tag.MAX_NAME
        }
        if (missing.isEmpty()) return 0
        tags.addImportedTags(missing.map { (id, use) -> Tag(id, TagIds.cleanName(use.label)) })
        return missing.size
    }
}
