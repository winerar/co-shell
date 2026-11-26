package app.coshell.core.domain.categoryguess

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * «Умная категория» (#584): picks the category of a new operation from past ones — same payee, same
 * seller of a scanned receipt, same first line of the note. Pure counting, no AI; the history is the
 * «500 latest» the entry sheet already loads.
 */
object CategoryGuess {
    /** How many latest operations are looked at: the entry sheet's «500 latest» (#742: 200 was about six weeks of a family's records). */
    const val WINDOW = 500

    /**
     * [history] newest first. Signals are tried in order — payee, receipt seller, note — and the first one
     * that finds a usable category wins; the most frequent category takes it, a tie goes to the newer one.
     * Archived, deleted (absent from [categories]) and wrong-type categories are never suggested; a transfer has none.
     */
    fun guess(
        history: List<Transaction>,
        type: TransactionType,
        categories: List<Category>,
        payee: String? = null,
        receiptKey: String? = null,
        note: String = "",
    ): String? {
        val wanted = when (type) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.TRANSFER -> return null
        }
        val usable = categories.filter { it.type == wanted && !it.archived }.map { it.id }.toSet()
        val past = history.asSequence().take(WINDOW).filter { it.type == type && it.categoryId in usable }.toList()
        val payeeKey = normalize(payee)
        val seller = seller(receiptKey)
        val noteKey = noteKey(note)
        return mostFrequent(past) { payeeKey != null && normalize(it.payee) == payeeKey }
            ?: mostFrequent(past) { seller != null && seller(it.receiptKey) == seller }
            ?: mostFrequent(past) { noteKey != null && noteKey(it.note) == noteKey }
    }

    /** Where a suggested category came from: the family's own past operations, or the brand dictionary (a payee never seen before). */
    enum class Source { HISTORY, DICTIONARY }

    class Suggestion(val categoryId: String, val source: Source)

    /**
     * History first ([guess]); for an expense nobody has recorded before — [BrandDictionary] by the payee, then by the first line of
     * the note («Starbucks», «farmacia»). A payee the dictionary does not know gets nothing: the person chooses.
     */
    fun suggest(
        history: List<Transaction>,
        type: TransactionType,
        categories: List<Category>,
        payee: String? = null,
        receiptKey: String? = null,
        note: String = "",
    ): Suggestion? {
        guess(history, type, categories, payee, receiptKey, note)?.let { return Suggestion(it, Source.HISTORY) }
        if (type != TransactionType.EXPENSE) return null
        val icon = BrandDictionary.iconOf(payee) ?: BrandDictionary.iconOf(note.lineSequence().firstOrNull { it.isNotBlank() })
        val uses = history.asSequence().take(WINDOW).filter { it.type == type }.mapNotNull { it.categoryId }.groupingBy { it }.eachCount()
        return BrandDictionary.categoryFor(icon, categories, uses)?.let { Suggestion(it, Source.DICTIONARY) }
    }

    private fun mostFrequent(past: List<Transaction>, matches: (Transaction) -> Boolean): String? {
        val found = past.filter(matches).mapNotNull { it.categoryId }
        if (found.isEmpty()) return null
        val counts = found.groupingBy { it }.eachCount()
        val best = counts.values.max()
        // `found` is newest first: the first category with the top count is the most recent one.
        return found.first { counts.getValue(it) == best }
    }

    /**
     * Name for comparison only (the stored one never changes): lower case, «MERPAGO*KIOSCO 123» → «kiosco»,
     * digits, punctuation and extra spaces dropped. `null` when nothing readable is left.
     */
    fun normalize(name: String?): String? {
        val tail = name?.substringAfterLast('*') ?: return null
        val cleaned = buildString {
            for (c in tail.lowercase()) append(if (c.isLetter()) c else ' ')
        }.trim().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
        return cleaned.ifEmpty { null }
    }

    /** Seller id (CUIT) of a receipt key `seller:point:type:number` (`ReceiptQr.key`). */
    fun seller(receiptKey: String?): String? = receiptKey?.substringBefore(':')?.takeIf { it.isNotBlank() }

    /** First line of a note — receipt notes start with the shop, then the items. */
    private fun noteKey(note: String): String? = normalize(note.lineSequence().firstOrNull { it.isNotBlank() })
}
