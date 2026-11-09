package app.coshell.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.data.family.FamilyState
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.family.components.CheckingBlock
import app.coshell.family.components.FamilyMessageCard
import app.coshell.family.components.InvitedBlock
import app.coshell.family.components.JoinModeDialog
import app.coshell.family.components.JoinedBlock
import app.coshell.family.components.NoAccessBlock
import app.coshell.family.components.NotConfiguredBlock
import app.coshell.family.components.OfflineBlock
import app.coshell.family.components.SignedOutBlock
import app.coshell.core.designsystem.R as DesignR

/** «Ещё → Семья и синхронизация»: one block per [FamilyState], then the last action's result. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(state: FamilyUiState, actions: FamilyActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (state.isBusy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = Spacing.l))
            when (val family = state.family) {
                FamilyState.NotConfigured -> NotConfiguredBlock()
                FamilyState.SignedOut -> SignedOutBlock(state.isBusy, actions.onSignIn)
                is FamilyState.Checking -> CheckingBlock(family.email)
                is FamilyState.NoAccess -> NoAccessBlock(family.email, state.isBusy, actions)
                is FamilyState.Invited -> InvitedBlock(family.email, state.isBusy, actions)
                is FamilyState.Offline -> OfflineBlock(family.email, state.isBusy, actions)
                is FamilyState.Joined -> JoinedBlock(family, state, actions)
            }
            state.message?.let { FamilyMessageCard(it, Modifier.padding(horizontal = Spacing.l)) }
        }
    }
    if (state.askJoinMode) JoinModeDialog(onChoose = actions.onJoinMode, onDismiss = actions.onDismissJoinMode)
}
