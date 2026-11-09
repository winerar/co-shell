package app.coshell.shake

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Length of the shake buzz: long enough to feel through a pocket, short enough not to annoy. */
private const val SHAKE_BUZZ_MS = 70L

/** The device vibrator, or null if there is none. */
fun shakeVibrator(context: Context): Vibrator? {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        context.getSystemService(Vibrator::class.java)
    }
    return vibrator?.takeIf { it.hasVibrator() }
}

/**
 * One firm buzz at full strength. Stronger than the UI "confirm" haptic, which some phones
 * (owner's Xiaomi) play too softly to notice after a shake.
 */
fun vibrateShake(vibrator: Vibrator) {
    val amplitude = if (vibrator.hasAmplitudeControl()) 255 else VibrationEffect.DEFAULT_AMPLITUDE
    vibrator.vibrate(VibrationEffect.createOneShot(SHAKE_BUZZ_MS, amplitude))
}
