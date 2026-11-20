package app.coshell.privacy.lock

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.privacy.pin.PinLayout
import app.coshell.privacy.pin.PinStep
import app.coshell.privacy.pin.toUi
import app.coshell.ui.form.ConfirmDialog

/**
 * Covers the whole app while it is locked (#145, board `Priv145LockA`). Opens the fingerprint window by itself when it is on;
 * «Забыли PIN?» → the phone's own lock → a new PIN. «Назад» leaves the app (to the home screen), it never opens it.
 */
@Composable
fun LockScreen(biometric: Boolean, viewModel: LockViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    val context = LocalContext.current
    val canBiometric = remember(biometric) { biometric && Biometrics.available(context) }
    var forgot by rememberSaveable { mutableStateOf(false) }
    val title = stringResource(R.string.lock_biometric_title)
    val usePin = stringResource(R.string.lock_biometric_use_pin)
    val askBiometric = { if (activity != null) Biometrics.prompt(activity, title, usePin, viewModel::unlockedByBiometric) }

    // Locked again after an unlock: start over at «Введите PIN» (a rotation keeps a half-typed new PIN).
    LaunchedEffect(Unit) { if (viewModel.state.value.done) viewModel.reset() }
    LaunchedEffect(canBiometric) { if (canBiometric && state.step == PinStep.ENTER) askBiometric() }
    BackHandler { activity?.moveTaskToBack(true) }

    Scaffold(Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { padding ->
        PinLayout(
            ui = state.toUi(),
            modifier = Modifier.padding(padding),
            onKey = { key -> if (key == PinKey.Biometric) askBiometric() else viewModel.key(key) },
            biometricLabel = if (canBiometric && state.step == PinStep.ENTER) stringResource(R.string.privacy_biometric) else null,
            link = if (state.step == PinStep.ENTER) stringResource(R.string.lock_forgot) to { forgot = true } else null,
        )
    }
    if (forgot) {
        if (Biometrics.deviceSecure(context)) {
            val confirmTitle = stringResource(R.string.lock_forgot)
            ConfirmDialog(
                title = R.string.lock_forgot,
                text = R.string.lock_forgot_text,
                confirmLabel = R.string.lock_forgot_continue,
                onConfirm = {
                    forgot = false
                    activity?.let { Biometrics.confirmDevice(it, confirmTitle, viewModel::deviceConfirmed) }
                },
                onDismiss = { forgot = false },
            )
        } else {
            ConfirmDialog(
                title = R.string.lock_forgot,
                text = R.string.lock_no_device_lock,
                confirmLabel = R.string.lock_ok,
                onConfirm = { forgot = false },
                onDismiss = { forgot = false },
            )
        }
    }
}
