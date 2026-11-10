package app.coshell.core.domain

/** What «Стереть все данные» would delete: live rows of each kind. */
data class DataCounts(val accounts: Int, val transactions: Int, val categories: Int, val manualRates: Int)

/** Deletes all synced data of the family (soft: the deletions sync to the other phone). */
interface DataWipeRepository {
    suspend fun counts(): DataCounts
    suspend fun wipeAll()
}

/**
 * «Стереть все данные» (feedback #107: load Wallet from scratch). A copy comes first: if [backup]
 * fails, nothing is deleted. After the wipe the default categories are seeded again.
 */
class WipeAllDataUseCase(
    private val wipe: DataWipeRepository,
    private val seed: SeedDefaultCategoriesUseCase,
) {
    suspend operator fun invoke(backup: suspend () -> Boolean): Boolean {
        if (!backup()) return false
        wipe.wipeAll()
        seed()
        return true
    }
}
