package app.coshell.categories.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.categories.list.components.archivedItems
import app.coshell.categories.list.components.groupItems
import app.coshell.core.designsystem.components.CoShellExtendedFab
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.SegmentTone
import app.coshell.core.designsystem.components.rememberSegmentSwipe
import app.coshell.core.designsystem.components.segmentSwipe
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.CategoryType
import app.coshell.navigation.CategoryEditDestination
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** "More → Categories": expense/income tabs, folders that expand, archive at the bottom. */
@Composable
fun CategoriesScreen(state: CategoriesUiState, actions: CategoriesActions) {
    val type = state.type.name
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.categories_title), onBack = actions.onBack)
        },
        floatingActionButton = {
            // One «+» for categories and folders: the kind is chosen in the editor (owner 2026-11-09).
            CoShellExtendedFab(
                onClick = { actions.onOpen(CategoryEditDestination(type = type)) },
                icon = { Icon(painterResource(DesignR.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.categories_add)) },
            )
        },
    ) { padding ->
        // Expenses / income: a way of looking at the list — the violet pill (К3); the list swipes too (#173).
        val types = CategoryType.entries
        val selected = types.indexOf(state.type)
        val onSelect = { index: Int -> actions.onSelectType(types[index]) }
        val swipe = rememberSegmentSwipe()
        Column(Modifier.fillMaxSize().padding(padding).segmentSwipe(swipe, selected, types.size, onSelect)) {
            CoShellSegmented(
                labels = types.map { stringResource(it.labelRes) },
                selected = selected,
                onSelect = onSelect,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.s),
                tone = SegmentTone.View,
                swipe = swipe,
            )
            if (state.isLoading) return@Column
            LazyColumn(contentPadding = PaddingValues(bottom = Sizes.fabClearance)) {
                groupItems(state, actions)
                archivedItems(state, actions)
            }
        }
    }
}
