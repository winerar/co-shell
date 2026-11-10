package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.CategoryChoice
import app.coshell.core.domain.importer.wallet.WalletCategoryKey
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.importer.wallet.WalletImportDraft
import app.coshell.core.designsystem.R as DesignR

/** Step 4: a row per Wallet category and direction; a tap opens [CategoryPickDialog]. */
fun LazyListScope.categoriesStep(draft: WalletImportDraft, existing: List<Category>, onOpen: (WalletCategoryKey) -> Unit) {
    item(key = "hint") { CoShellHint(stringResource(R.string.import_categories_hint), Modifier.padding(horizontal = Spacing.l)) }
    items(draft.categoryKeys, key = { "cat-${it.name}-${it.type}" }) { key ->
        val target = when (val choice = draft.categoryChoices[key]) {
            is CategoryChoice.UseExisting -> existing.firstOrNull { it.id == choice.categoryId }?.name
            is CategoryChoice.CreateNew -> stringResource(R.string.import_category_new, choice.name)
            null -> null
        }
        CoShellListRow(
            title = key.name,
            subtitle = stringResource(R.string.import_category_records, typeLabel(key.type), draft.categoryCounts[key] ?: 0) +
                (target?.let { " → $it" } ?: ""),
            trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
            onClick = { onOpen(key) },
        )
    }
}

/** «Новая: X» first, then live categories of the same type (subcategories named «Папка / X»). */
@Composable
fun CategoryPickDialog(
    key: WalletCategoryKey,
    selected: CategoryChoice?,
    existing: List<Category>,
    onPick: (CategoryChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    val byId = existing.associateBy { it.id }
    val options = listOf(ChoiceOption<CategoryChoice>(CategoryChoice.CreateNew(key.name, key.type), stringResource(R.string.import_category_new, key.name))) +
        existing.filter { it.type == key.type && !it.archived }.map { c ->
            val name = c.parentId?.let(byId::get)?.let { "${it.name} / ${c.name}" } ?: c.name
            ChoiceOption<CategoryChoice>(CategoryChoice.UseExisting(c.id), name)
        }.sortedBy { it.label.lowercase() }
    ChoiceDialog(stringResource(R.string.import_category_pick_title, key.name), options, selected, onPick, onDismiss)
}

@Composable
private fun typeLabel(type: CategoryType) =
    stringResource(if (type == CategoryType.EXPENSE) R.string.import_expense else R.string.import_income)
