package app.coshell.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.R as DesignR

// Stateless menu tab: categories now, settings and backup later. No ViewModel needed yet.

@Composable
fun MoreRoute(onCategories: () -> Unit) = MoreScreen(onCategories)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onCategories: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_more)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.categories_title)) },
                    leadingContent = { Icon(painterResource(DesignR.drawable.ic_category), contentDescription = null) },
                    trailingContent = {
                        Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onCategories),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { MoreScreen(onCategories = {}) }
}
