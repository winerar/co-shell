package app.coshell.access

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Wraps the app (#666): while the code is being read nothing is drawn; without a valid code — the code screen instead of
 * the app (also on request, from «Доступ закрыт» and «Настройки → Помощь», #671); otherwise the app. The app's saved state is
 * kept ([rememberSaveableStateHolder], as `PrivacyHost`): after «Отмена» or a new code the same screen comes back. Builds without the gate are always open. The server is
 * asked about the code again whenever the app comes to the foreground and [app.coshell.core.domain.access.AccessGate.RECHECK] has passed.
 */
@Composable
fun AccessHost(viewModel: AccessViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val entering by viewModel.entering.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.start() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.recheck() }
    val saved = rememberSaveableStateHolder()
    when {
        state is AccessState.Loading -> Unit
        state is AccessState.Required || entering -> {
            // From «Доступ закрыт» the person may go back to their data; the very first code has nowhere to go back to.
            val canCancel = state !is AccessState.Required
            if (canCancel) BackHandler(onBack = viewModel::cancelEntering)
            AccessScreen(entry, viewModel::submit, onCancel = if (canCancel) viewModel::cancelEntering else null)
        }
        else -> saved.SaveableStateProvider(APP_KEY) { content() }
    }
}

private const val APP_KEY = "app"
