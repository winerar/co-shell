package app.coshell.privacy

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.shake.FaceDownDetector

/**
 * While [enabled] and the app is on screen, listens to the accelerometer (the UI rate is enough for a slow pose) and calls
 * [onFaceDown] when the phone lies screen down ([FaceDownDetector], #324). Released in the background — no battery there.
 */
@Composable
fun FaceDownEffect(enabled: Boolean, onFaceDown: () -> Unit) {
    if (!enabled) return
    val context = LocalContext.current
    val current by rememberUpdatedState(onFaceDown)
    LifecycleResumeEffect(context) {
        val sensors = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = FaceDownDetector()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                if (detector.onSample(x, y, z, event.timestamp) && detector.isFaceDown) current()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        accelerometer?.let { sensors.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onPauseOrDispose { sensors?.unregisterListener(listener) }
    }
}

/**
 * «Прятать в недавних» and «Запретить скриншоты» on the window (#145). Android 13+ can blank only the recents card
 * (`setRecentsScreenshotEnabled`), screenshots stay possible; older phones can only do both with `FLAG_SECURE`.
 */
@Composable
fun WindowPrivacyEffect(activity: Activity, options: PrivacyOptions) {
    val secure = options.blockScreenshots || (options.hideInRecents && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    DisposableEffect(secure, options.previewHidden) {
        if (secure) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) activity.setRecentsScreenshotEnabled(!options.previewHidden)
        onDispose {}
    }
}
