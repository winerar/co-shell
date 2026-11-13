package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.tags.EnsureLabelTagsUseCase
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.EnsureAdjustmentCategoriesUseCase
import app.coshell.core.domain.importer.ImportRepository
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.SystemCategories
import kotlinx.coroutines.flow.first

/**
 * The Wallet wizard's two calls (stage 5c): [preview] reads the current data and plans, [save]
 * writes the plan in one transaction. Re-importing the same or a newer file adds only new records.
 */
class WalletImportUseCase(
    private val accounts: AccountRepository,
    private val imports: ImportRepository,
    private val ensureAdjustment: EnsureAdjustmentCategoriesUseCase,
    ids: IdGenerator = IdGenerator.Random,
    private val newAccountColor: NewAccountColor = NewAccountColor.None,
    /** Labels of the new operations become tags (#129); `null` — skipped (tests that do not care). */
    private val ensureLabelTags: EnsureLabelTagsUseCase? = null,
) {
    private val planner = WalletImportPlanner(ids)

    suspend fun preview(records: List<WalletRecord>, choices: WalletImportChoices): WalletImportPlan =
        planner.plan(
            records = records,
            choices = choices,
            existing = accounts.observeAccountsWithBalance().first(),
            adjustmentCategories = CategoryType.entries.associateWith(SystemCategories::adjustmentId),
            known = imports.knownExternalIds(WalletImportBuilder.SOURCE),
            color = newAccountColor,
        )

    /**
     * Creates «Корректировка баланса» if needed (the plan may use it), then saves everything at once and
     * makes tags of the new labels.
     */
    suspend fun save(plan: WalletImportPlan) {
        if (plan.write.let { it.transactions.isEmpty() && it.accounts.isEmpty() && it.categories.isEmpty() }) return
        ensureAdjustment()
        imports.save(plan.write)
        ensureLabelTags?.invoke()
    }
}
