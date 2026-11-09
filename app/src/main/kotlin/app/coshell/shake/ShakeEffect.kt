package app.coshell.shake

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.coshell.core.domain.shake.ShakeDetector

/**
 * While [enabled] and the app is on screen (lifecycle RESUMED), listens to the accelerometer;
 * on a shake ([ShakeDetector]) gives a firm short buzz ([vibrateShake]) and calls [onShake]. The sensor is released
 * as soon as the app goes to the background or [enabled] turns off, so it costs no battery there.
 */
@Composable
fun ShakeEffect(enabled: Boolean, onShake: () -> Unit) {
    if (!enabled) return
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)

    LifecycleResumeEffect(context) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val vibrator = shakeVibrator(context)
        val detector = ShakeDetector()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                if (detector.onSample(x, y, z, event.timestamp)) {
                    vibrator?.let(::vibrateShake)
                    currentOnShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        // GAME rate (~50 Hz) catches short jolts that the slower UI rate can miss between samples.
        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onPauseOrDispose { sensorManager?.unregisterListener(listener) }
    }
}
