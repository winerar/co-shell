package app.coshell.reports.recap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/** Connects [MonthRecapScreen] to [MonthRecapViewModel]; «Подробнее» on the last story opens the review of the month, the top-bar icon shares a picture. */
@Composable
fun MonthRecapRoute(onBack: () -> Unit, onDetails: () -> Unit, viewModel: MonthRecapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    MonthRecapScreen(state, onBack, onDetails, onShare = { picture ->
        scope.launch { shareRecapPicture(context, picture, "co-shell-recap.png") }
    })
}
