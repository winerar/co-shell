package app.coshell.templates.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.templates.components.TemplateChip
import app.coshell.templates.list.components.TemplateRow
import app.coshell.templates.list.components.TypeFilterChips
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/**
 * «Все шаблоны» (mockup `TplAll`): search, «Часто», type chips, then templates by type and category.
 * A tap fills the «+» sheet from the template, a long press opens its editor.
 */
@Composable
fun TemplatesScreen(state: TemplatesUiState, actions: TemplatesActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.templates_title), onBack = actions.onBack) {
                CoShellIconButton(onClick = actions.onNew) {
                    Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.template_new))
                }
            }
        },
    ) { padding ->
        if (state.isEmpty) {
            EmptyTemplates(actions.onNew, Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            item(key = "search") {
                CoShellTextField(
                    value = state.query,
                    onValueChange = actions.onQueryChange,
                    label = stringResource(R.string.templates_search, state.total),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                )
            }
            if (state.frequent.isNotEmpty()) {
                item(key = "frequent") {
                    SectionTitle(stringResource(R.string.templates_frequent))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        state.frequent.forEach { item -> TemplateChip(item, onClick = { actions.onUse(item.id) }) }
                    }
                }
            }
            if (state.counts.size > 1) {
                item(key = "filter") {
                    TypeFilterChips(state.total, state.counts, state.filter, actions.onFilterChange, Modifier.padding(top = Spacing.m))
                }
            }
            state.sections.forEach { section ->
                item(key = "title-${section.type}") { SectionTitle(stringResource(sectionTitle(section.type))) }
                items(section.items, key = { it.id }) { item ->
                    TemplateRow(item, onClick = { actions.onUse(item.id) }, onLongClick = { actions.onEdit(item.id) })
                }
            }
            item(key = "footer") {
                Text(
                    stringResource(if (state.nothingFound) R.string.picker_nothing else R.string.templates_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.l),
                )
            }
        }
    }
}

@Composable
private fun EmptyTemplates(onNew: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Text(
            stringResource(R.string.templates_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CoShellButton(onClick = onNew) { Text(stringResource(R.string.template_new)) }
    }
}

private fun sectionTitle(type: TransactionType): Int = when (type) {
    TransactionType.EXPENSE -> R.string.templates_section_expense
    TransactionType.INCOME -> R.string.templates_section_income
    TransactionType.TRANSFER -> R.string.templates_section_transfer
}
