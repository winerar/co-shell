package app.coshell.newmark

/**
 * Places that wear the «новое» pill after an update (#335): a key per place, added with the feature in the same change
 * as its `docs/whats-new.md` item. Remove a key a month after its release — the mark has long expired by then.
 */
object NewFeatures {
    /** «Ещё → Регулярные платежи» (#403). */
    const val RECURRING = "more.recurring"

    val all: List<String> = listOf(RECURRING)
}
