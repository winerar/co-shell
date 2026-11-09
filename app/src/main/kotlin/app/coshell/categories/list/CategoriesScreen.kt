package app.coshell.categories.list

import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.categories.list.components.archivedItems
import app.coshell.categories.list.components.groupItems
import app.coshell.core.designsystem.components.CoShellFab
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.accentInk
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
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                actions = {
                    CoShellIconButton(onClick = { actions.onOpen(CategoryEditDestination(type = type, asFolder = true)) }) {
                        Icon(
                            painterResource(DesignR.drawable.ic_create_new_folder),
                            stringResource(R.string.categories_add_folder),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            CoShellFab(onClick = { actions.onOpen(CategoryEditDestination(type = type)) }) {
                Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.categories_add))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Text accent: ink in the light theme, Neon Mint in the dark one (no deep green).
            val ink = accentInk()
            PrimaryTabRow(
                selectedTabIndex = state.type.ordinal,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(state.type.ordinal, matchContentSize = true),
                        color = ink,
                    )
                },
            ) {
                CategoryType.entries.forEach { tabType ->
                    Tab(
                        selected = state.type == tabType,
                        selectedContentColor = ink,
                        onClick = { actions.onSelectType(tabType) },
                        text = { Text(stringResource(tabType.labelRes)) },
                    )
                }
            }
            if (state.isLoading) return@Column
            LazyColumn(contentPadding = PaddingValues(bottom = Sizes.fabClearance)) {
                groupItems(state, actions)
                archivedItems(state, actions)
            }
        }
    }
}
