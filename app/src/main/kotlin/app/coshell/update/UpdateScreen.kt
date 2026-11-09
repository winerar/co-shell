package app.coshell.update

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.update.components.ReleaseCard
import app.coshell.update.components.UpdateHero
import app.coshell.update.components.UpdateMainAction
import app.coshell.update.components.UpdatePhaseContent
import app.coshell.core.designsystem.R as DesignR

/**
 * "More → App update" (issue #65): a big ring in the middle shows the step (check, download %, install),
 * under it the title and the release notes; the one action sits full width at the bottom, under the thumb.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(state: UpdateUiState, actions: UpdateActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.update_title)) },
                navigationIcon = {
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
        bottomBar = {
            UpdateMainAction(state, actions, Modifier.navigationBarsPadding().padding(horizontal = Spacing.l, vertical = Spacing.m))
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.l, vertical = Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Spacer(Modifier.height(Spacing.xl))
            UpdateHero(state.phase)
            UpdatePhaseContent(state, actions)
            val notes = state.phase.release?.takeIf { it.notes.isNotBlank() && state.phase !is UpdatePhase.Failed }
            CoShellExpandable(expanded = notes != null) {
                notes?.let { ReleaseCard(it) }
            }
        }
    }
}
