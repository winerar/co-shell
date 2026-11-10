package app.coshell.wipe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.DataCounts
import app.coshell.ui.top.ScreenTopBar
import app.coshell.wipe.WipeDataUiState.Step
import app.coshell.wipe.components.WipeConfirm
import app.coshell.wipe.components.WipeDone

/** «Настройки → Стереть все данные» (board 18). [onImport] — «Импорт из Wallet» after the wipe. */
@Composable
fun WipeDataRoute(onBack: () -> Unit, onImport: () -> Unit, viewModel: WipeDataViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WipeDataScreen(state, onBack, viewModel::onWordChange, viewModel::wipe, onImport)
}

@Composable
fun WipeDataScreen(
    state: WipeDataUiState,
    onBack: () -> Unit = {},
    onWordChange: (String) -> Unit = {},
    onWipe: () -> Unit = {},
    onImport: () -> Unit = {},
) {
    // No way out halfway through: the copy is written, the wipe is one transaction.
    BackHandler(enabled = state.step == Step.WIPING) {}
    Scaffold(
        topBar = {
            ScreenTopBar(
                stringResource(R.string.wipe_title),
                onBack = onBack.takeIf { state.step != Step.DONE },
                backEnabled = state.step != Step.WIPING,
            )
        },
    ) { padding ->
        val content = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Spacing.l)
        if (state.step == Step.DONE) {
            WipeDone(state.backupName, onImport, onBack, content)
        } else {
            WipeConfirm(state, onWordChange, onWipe, content)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun WipeDataScreenPreview() {
    CoShellTheme(darkTheme = true) { WipeDataScreen(WipeDataUiState(counts = DataCounts(12, 3418, 46, 8), word = "СТЕР")) }
}

@Preview(showBackground = true)
@Composable
private fun WipeDataDonePreview() {
    CoShellTheme { WipeDataScreen(WipeDataUiState(step = Step.DONE, backupName = "co-shell-before-wipe-2026-11-10-0912.json")) }
}
