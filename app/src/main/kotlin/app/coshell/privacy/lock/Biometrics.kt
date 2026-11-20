package app.coshell.privacy.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * The system fingerprint/face window (#145, board `Priv145Bio`) and the phone's own lock for «Забыли PIN?».
 * `BIOMETRIC_WEAK`: face unlock on most phones is «weak»; nothing is encrypted with it — it only opens the lock screen.
 */
object Biometrics {

    /** The phone has a fingerprint or face enrolled. */
    fun available(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    /** The phone has its own screen lock (PIN, pattern, password or biometrics) to confirm «Забыли PIN?». */
    fun deviceSecure(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS

    /** Fingerprint or face; [negative] — the «Ввести PIN» button that closes the window. */
    fun prompt(activity: FragmentActivity, title: String, negative: String, onSuccess: () -> Unit) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negative)
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()
        prompt(activity, onSuccess).authenticate(info)
    }

    /** The phone's own lock (any of its methods): proves it is the owner, then a new PIN can be set. */
    fun confirmDevice(activity: FragmentActivity, title: String, onSuccess: () -> Unit) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()
        prompt(activity, onSuccess).authenticate(info)
    }

    private fun prompt(activity: FragmentActivity, onSuccess: () -> Unit) =
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            },
        )
}
