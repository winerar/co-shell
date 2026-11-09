package app.coshell.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.BuildConfig
import app.coshell.family.data.signInWithGoogle
import kotlinx.coroutines.launch

/** Connects [FamilyScreen] to its ViewModel and to the system Google account picker. */
@Composable
fun FamilyRoute(onBack: () -> Unit, viewModel: FamilyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FamilyScreen(
        state = state,
        actions = FamilyActions(
            onBack = onBack,
            onSignIn = {
                scope.launch { viewModel.onGoogleResult(signInWithGoogle(context, BuildConfig.GOOGLE_WEB_CLIENT_ID)) }
            },
            onRetry = viewModel::retry,
            onCreateFamily = viewModel::createFamily,
            onJoin = viewModel::join,
            onJoinMode = viewModel::chooseJoinMode,
            onDismissJoinMode = viewModel::dismissJoinMode,
            onInvite = viewModel::invite,
            onCancelInvite = viewModel::cancelInvite,
            onSyncNow = viewModel::syncNow,
            onSignOut = viewModel::signOut,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class FamilyActions(
    val onBack: () -> Unit = {},
    val onSignIn: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onCreateFamily: () -> Unit = {},
    val onJoin: () -> Unit = {},
    val onJoinMode: (keepLocalData: Boolean) -> Unit = {},
    val onDismissJoinMode: () -> Unit = {},
    val onInvite: (email: String) -> Unit = {},
    val onCancelInvite: (email: String) -> Unit = {},
    val onSyncNow: () -> Unit = {},
    val onSignOut: () -> Unit = {},
)
