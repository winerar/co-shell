package app.coshell.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.CategoryType
import app.coshell.navigation.CategoryEditDestination
import app.coshell.core.designsystem.R as DesignR

@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    onOpen: (CategoryEditDestination) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoriesScreen(
        state = state,
        onBack = onBack,
        onSelectType = viewModel::selectType,
        onToggleFolder = viewModel::toggleFolder,
        onToggleArchived = viewModel::toggleArchived,
        onOpen = onOpen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    state: CategoriesUiState,
    onBack: () -> Unit,
    onSelectType: (CategoryType) -> Unit,
    onToggleFolder: (String) -> Unit,
    onToggleArchived: () -> Unit,
    onOpen: (CategoryEditDestination) -> Unit,
) {
    val type = state.type.name
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onOpen(CategoryEditDestination(type = type, asFolder = true)) }) {
                        Icon(
                            painterResource(DesignR.drawable.ic_create_new_folder),
                            stringResource(R.string.categories_add_folder),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpen(CategoryEditDestination(type = type)) }) {
                Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.categories_add))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = state.type.ordinal) {
                CategoryType.entries.forEach { tabType ->
                    Tab(
                        selected = state.type == tabType,
                        onClick = { onSelectType(tabType) },
                        text = { Text(stringResource(tabType.labelRes)) },
                    )
                }
            }
            if (state.isLoading) return@Column
            LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                state.groups.forEach { group ->
                    item(key = group.item.id) {
                        GroupRow(
                            group = group,
                            onClick = { onOpen(CategoryEditDestination(categoryId = group.item.id)) },
                            onToggle = { onToggleFolder(group.item.id) },
                        )
                    }
                    if (group.isFolder && group.expanded) {
                        items(group.children, key = { it.id }) { child ->
                            CategoryRow(
                                item = child,
                                indent = true,
                                onClick = { onOpen(CategoryEditDestination(categoryId = child.id)) },
                            )
                        }
                        item(key = "add-to-${group.item.id}") {
                            TextButton(
                                onClick = { onOpen(CategoryEditDestination(type = type, parentId = group.item.id)) },
                                modifier = Modifier.padding(start = 56.dp),
                            ) { Text(stringResource(R.string.categories_add_to_folder)) }
                        }
                    }
                }
                if (state.archived.isNotEmpty()) {
                    item(key = "archived-toggle") {
                        TextButton(onClick = onToggleArchived, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                stringResource(
                                    if (state.showArchived) {
                                        R.string.categories_hide_archived
                                    } else {
                                        R.string.categories_show_archived
                                    },
                                    state.archived.size,
                                ),
                            )
                        }
                    }
                    if (state.showArchived) {
                        items(state.archived, key = { "archived-${it.id}" }) { item ->
                            CategoryRow(item, indent = false, onClick = {
                                onOpen(CategoryEditDestination(categoryId = item.id))
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupRow(group: CategoriesUiState.Group, onClick: () -> Unit, onToggle: () -> Unit) {
    if (!group.isFolder) {
        CategoryRow(group.item, indent = false, onClick = onClick)
        return
    }
    ListItem(
        headlineContent = { Text(group.item.name) },
        supportingContent = {
            Text(pluralStringResource(R.plurals.categories_in_folder, group.children.size, group.children.size))
        },
        leadingContent = {
            Icon(
                painterResource(DesignR.drawable.ic_folder),
                contentDescription = null,
                tint = group.item.color?.let(::Color) ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(onClick = onToggle) {
                Icon(
                    painterResource(DesignR.drawable.ic_keyboard_arrow_down),
                    contentDescription = stringResource(
                        if (group.expanded) R.string.categories_collapse else R.string.categories_expand,
                    ),
                    modifier = Modifier.rotate(if (group.expanded) 180f else 0f),
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun CategoryRow(item: CategoriesUiState.Item, indent: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        leadingContent = {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(item.color?.let(::Color) ?: MaterialTheme.colorScheme.outline, CircleShape),
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick).padding(start = if (indent) 40.dp else 0.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() {
    CoShellTheme {
        CategoriesScreen(
            state = CategoriesUiState(
                groups = listOf(
                    CategoriesUiState.Group(
                        CategoriesUiState.Item("car", "Машина", 0xFF2196F3),
                        children = listOf(
                            CategoriesUiState.Item("fuel", "Бензин", 0xFFFFC107),
                            CategoriesUiState.Item("parking", "Парковка", null),
                        ),
                        expanded = true,
                    ),
                    CategoriesUiState.Group(CategoriesUiState.Item("food", "Продукты", 0xFF4CAF50), emptyList(), false),
                ),
                archived = listOf(CategoriesUiState.Item("old", "Старое", null)),
            ),
            onBack = {},
            onSelectType = {},
            onToggleFolder = {},
            onToggleArchived = {},
            onOpen = {},
        )
    }
}
