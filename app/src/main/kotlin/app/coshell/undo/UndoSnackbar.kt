package app.coshell.undo

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.coshell.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Gives the app shell the [UndoCenter]; undo runs in this scope, so it finishes even if the screen changes. */
@HiltViewModel
class UndoViewModel @Inject constructor(val center: UndoCenter) : ViewModel() {
    fun undo(offer: UndoOffer) {
        viewModelScope.launch { offer.undo() }
    }
}

/** Shows each [UndoOffer] as «message · Отменить» in [host]; a newer offer replaces the shown one. */
@Composable
fun UndoSnackbarEffect(host: SnackbarHostState, viewModel: UndoViewModel = hiltViewModel()) {
    val resources = LocalResources.current
    LaunchedEffect(viewModel, resources) {
        viewModel.center.offers.collectLatest { offer ->
            val result = host.showSnackbar(
                message = resources.getString(offer.message, *offer.args.toTypedArray()),
                actionLabel = resources.getString(R.string.action_undo),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(offer)
        }
    }
}
