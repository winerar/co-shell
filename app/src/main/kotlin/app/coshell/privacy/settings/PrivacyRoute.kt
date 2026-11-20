package app.coshell.privacy.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.privacy.PrivacyCenter
import app.coshell.privacy.lock.Biometrics
import app.coshell.privacy.pin.PinSetupMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyViewModel @Inject constructor(private val center: PrivacyCenter) : ViewModel() {
    val options = center.options

    fun change(f: (PrivacyOptions) -> PrivacyOptions) {
        viewModelScope.launch { center.change(f) }
    }
}

/** «Ещё → Конфиденциальность»; [onPinSetup] opens the PIN screen in the given mode. */
@Composable
fun PrivacyRoute(onBack: () -> Unit, onPinSetup: (PinSetupMode) -> Unit, viewModel: PrivacyViewModel = hiltViewModel()) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometric = remember { Biometrics.available(context) }
    PrivacyScreen(
        options = options ?: PrivacyOptions(),
        biometricAvailable = biometric,
        actions = PrivacyActions(
            onPin = { on -> onPinSetup(if (on) PinSetupMode.CREATE else PinSetupMode.DISABLE) },
            onChangePin = { onPinSetup(PinSetupMode.CHANGE) },
            change = viewModel::change,
            onBack = onBack,
        ),
    )
}
