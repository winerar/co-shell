package app.coshell.core.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Client-side ids (UUID v4): records can be created offline without asking a server. */
fun interface IdGenerator {
    fun newId(): String

    companion object {
        @OptIn(ExperimentalUuidApi::class)
        val Random: IdGenerator = IdGenerator { Uuid.random().toString() }
    }
}
