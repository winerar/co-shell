package app.coshell.core.domain.quickentry

/** Where the one [EntryDraft] lives (#323): on this phone only, never synced. Calls are cheap (a small file), so no coroutines. */
interface EntryDraftStore {
    fun load(): EntryDraft?
    fun save(draft: EntryDraft)
    fun clear()
}

/** For tests and previews. */
class InMemoryEntryDraftStore(private var draft: EntryDraft? = null) : EntryDraftStore {
    override fun load(): EntryDraft? = draft
    override fun save(draft: EntryDraft) {
        this.draft = draft
    }
    override fun clear() {
        draft = null
    }
}
