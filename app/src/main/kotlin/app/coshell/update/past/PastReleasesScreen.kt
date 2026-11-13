package app.coshell.update.past

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.ScreenTopBar

/** Connects [PastReleasesScreen] to its ViewModel. */
@Composable
fun PastReleasesRoute(onBack: () -> Unit, viewModel: PastReleasesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PastReleasesScreen(state, onBack = onBack, onRetry = viewModel::load)
}

/**
 * «Прошлые версии» (#284, board Fb284A): the last 20 releases as cards, the newest one open,
 * the others show their first point; a tap opens or closes a card.
 */
@Composable
fun PastReleasesScreen(state: PastReleasesState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.update_past_title), onBack = onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                PastReleasesState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                PastReleasesState.Failed -> Message(
                    stringResource(R.string.update_past_error),
                    action = { CoShellButton(onClick = onRetry) { Text(stringResource(R.string.update_past_retry)) } },
                )
                is PastReleasesState.Loaded ->
                    if (state.releases.isEmpty()) Message(stringResource(R.string.update_past_empty)) else ReleaseList(state)
            }
        }
    }
}

@Composable
private fun ReleaseList(state: PastReleasesState.Loaded) {
    // Open cards by versionCode; the newest starts open (board Fb284A). A list: saveable as is.
    var open by rememberSaveable { mutableStateOf(listOf(state.releases.first().versionCode)) }
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        item(key = "hint") {
            Text(
                stringResource(R.string.update_past_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            )
        }
        items(state.releases, key = { it.versionCode }) { release ->
            PastReleaseCard(
                release = release,
                isInstalled = release.versionCode == state.installedVersionCode,
                expanded = release.versionCode in open,
                onToggle = { open = if (release.versionCode in open) open - release.versionCode else open + release.versionCode },
            )
        }
    }
}

@Composable
private fun Message(text: String, action: (@Composable () -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.l, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        action?.invoke()
    }
}
