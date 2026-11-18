package app.coshell.converter

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/** Opens the converter sheet from any screen (#319): the entry rows of «Обзор», «Ещё → Деньги» and «Курсы» navigate here. */
@Composable
fun ConverterRoute(onClose: () -> Unit, viewModel: ConverterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = ConverterActions(
        onKey = viewModel::onKey,
        onActivate = viewModel::onActivate,
        onSwap = viewModel::onSwap,
        onPick = viewModel::onPick,
        onSource = viewModel::onSource,
        onMode = viewModel::onMode,
    )
    ConverterSheetBody(state, actions, onClose)
}
