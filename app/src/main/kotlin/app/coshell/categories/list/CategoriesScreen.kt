package app.coshell.categories.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.categories.list.components.archivedItems
import app.coshell.categories.list.components.groupItems
import app.coshell.core.model.CategoryType
import app.coshell.navigation.CategoryEditDestination
import app.coshell.core.designsystem.R as DesignR

/** "More → Categories": expense/income tabs, folders that expand, archive at the bottom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(state: CategoriesUiState, actions: CategoriesActions) {
    val type = state.type.name
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { actions.onOpen(CategoryEditDestination(type = type, asFolder = true)) }) {
                        Icon(
                            painterResource(DesignR.drawable.ic_create_new_folder),
                            stringResource(R.string.categories_add_folder),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { actions.onOpen(CategoryEditDestination(type = type)) }) {
                Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.categories_add))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = state.type.ordinal) {
                CategoryType.entries.forEach { tabType ->
                    Tab(
                        selected = state.type == tabType,
                        onClick = { actions.onSelectType(tabType) },
                        text = { Text(stringResource(tabType.labelRes)) },
                    )
                }
            }
            if (state.isLoading) return@Column
            LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                groupItems(state, actions)
                archivedItems(state, actions)
            }
        }
    }
}
