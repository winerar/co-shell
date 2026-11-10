package app.coshell.wipe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.DataCounts
import app.coshell.wipe.WipeDataUiState.Step
import app.coshell.wipe.components.WipeConfirm
import app.coshell.wipe.components.WipeDone
import app.coshell.core.designsystem.R as DesignR

/** «Настройки → Стереть все данные» (board 18). [onImport] — «Импорт из Wallet» after the wipe. */
@Composable
fun WipeDataRoute(onBack: () -> Unit, onImport: () -> Unit, viewModel: WipeDataViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WipeDataScreen(state, onBack, viewModel::onWordChange, viewModel::wipe, onImport)
}

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text(stringResource(R.string.wipe_title)) },
                navigationIcon = {
                    if (state.step != Step.DONE) {
                        CoShellIconButton(onClick = onBack, enabled = state.step != Step.WIPING) {
                            Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                        }
                    }
                },
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
