package app.coshell.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.ScreenTopBar
import app.coshell.update.components.UpdateDetails
import app.coshell.update.components.UpdateHero
import app.coshell.update.components.UpdateMainAction
import app.coshell.update.components.UpdatePhaseContent

/**
 * "More → App update" (issue #65): a big ring in the middle shows the step (check, download %, install),
 * under it the title, the version cards and the release notes (#268); the action sits full width at the bottom,
 * under the thumb, with «Не ставится? Установить вручную» under «Обновить» (#269).
 */
@Composable
fun UpdateScreen(state: UpdateUiState, actions: UpdateActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.update_title), onBack = actions.onBack)
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
            UpdateDetails(state)
        }
    }
}
