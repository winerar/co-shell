package app.coshell.templates

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.templates.TemplateUsage
import app.coshell.core.model.MoneyFormat
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Live templates as [TemplateItem]s plus how often each was used on this phone — the one source for
 * the strip in the «+» sheet and for «Все шаблоны».
 */
class TemplateCatalog @Inject constructor(
    private val templates: TemplateRepository,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val usage: TemplateUsage,
    private val moneyFormat: MoneyFormat,
) {
    /** [items] — in the templates' own order (sort order, then name). */
    data class Snapshot(val items: List<TemplateItem> = emptyList(), val counts: Map<String, Int> = emptyMap())

    fun observe(): Flow<Snapshot> = combine(
        templates.observeTemplates(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        usage.counts,
    ) { list, accountList, categoryList, counts ->
        val accountsById = accountList.associateBy { it.id }
        val categoriesById = categoryList.associateBy { it.id }
        Snapshot(list.map { it.toItem(accountsById, categoriesById, moneyFormat) }, counts)
    }
}
