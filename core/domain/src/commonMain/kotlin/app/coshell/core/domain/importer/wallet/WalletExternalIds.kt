package app.coshell.core.domain.importer.wallet

/**
 * Stable ids of Wallet records for `importExternalId` (the CSV has no ids): a hash of the record's
 * fields plus its occurrence among identical records, counted from the **oldest** — a newer export
 * only adds records at the new end, so ids of known records do not change and re-import skips them.
 */
object WalletExternalIds {

    fun of(records: List<WalletRecord>): Map<WalletRecord, String> {
        val seen = HashMap<String, Int>()
        val ids = HashMap<WalletRecord, String>(records.size * 2)
        records.sortedWith(compareBy<WalletRecord> { it.dateTime }.thenByDescending { it.line }).forEach { r ->
            val key = r.fingerprint()
            val n = seen.getOrElse(key) { 0 }
            seen[key] = n + 1
            ids[r] = fnv1a64("$key#$n")
        }
        return ids
    }

    /** A transfer made of two records gets one id built from both. */
    fun pair(fromId: String, toId: String): String = fnv1a64("$fromId>$toId")

    private fun WalletRecord.fingerprint() = listOf(
        account, category, amount.currency.value, amount.amountMinor.toString(), dateTime.toString(), note, label.orEmpty(),
    ).joinToString("\u001F")

    /** FNV-1a 64-bit as 16 hex digits: pure Kotlin, collisions are negligible for thousands of records. */
    internal fun fnv1a64(text: String): String {
        var hash = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
        for (b in text.encodeToByteArray()) {
            hash = hash xor (b.toLong() and 0xFF)
            hash *= 0x100000001b3L
        }
        return hash.toULong().toString(16).padStart(16, '0')
    }
}
