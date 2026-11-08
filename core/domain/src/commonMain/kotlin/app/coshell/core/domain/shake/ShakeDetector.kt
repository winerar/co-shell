package app.coshell.core.domain.shake

/**
 * Recognises a deliberate shake in a stream of accelerometer samples (pure logic, no sensors).
 * The Android side (`app/.../shake/ShakeEffect.kt`) feeds it raw readings and reacts to `true`.
 *
 * A shake is [spikesToShake] strong jolts (total acceleration above [thresholdG], gravity included)
 * within [windowMillis]. One bump or a phone dropped on the table gives a single jolt and is ignored.
 * Samples of the same jolt closer than [minSpikeGapMillis] count once; after a shake further jolts
 * are ignored for [cooldownMillis], so one long shake opens the screen once.
 */
class ShakeDetector(
    thresholdG: Float = DEFAULT_THRESHOLD_G,
    private val spikesToShake: Int = 2,
    private val minSpikeGapMillis: Long = 100,
    private val windowMillis: Long = 700,
    private val cooldownMillis: Long = 2_000,
) {
    // Compared squared to skip a square root per sample (the sensor delivers ~50 samples a second).
    private val thresholdSquared = (thresholdG * STANDARD_GRAVITY).let { it * it }

    private var firstSpikeAt = NONE
    private var lastSpikeAt = NONE
    private var spikes = 0
    private var shakeAt = NONE

    /**
     * Takes one reading: acceleration in m/s² on each axis and the sample time in nanoseconds
     * (any monotonic clock). Returns `true` exactly when this sample completes a shake.
     */
    fun onSample(x: Float, y: Float, z: Float, timestampNanos: Long): Boolean {
        val now = timestampNanos / NANOS_PER_MILLI
        if (x * x + y * y + z * z < thresholdSquared) return false
        if (shakeAt != NONE && now - shakeAt < cooldownMillis) return false
        if (lastSpikeAt != NONE && now - lastSpikeAt < minSpikeGapMillis) return false

        if (spikes == 0 || now - firstSpikeAt > windowMillis) {
            firstSpikeAt = now
            spikes = 0
        }
        spikes++
        lastSpikeAt = now
        if (spikes < spikesToShake) return false

        shakeAt = now
        spikes = 0
        return true
    }

    /** Forgets pending jolts, e.g. when listening stops and resumes later. */
    fun reset() {
        firstSpikeAt = NONE
        lastSpikeAt = NONE
        spikes = 0
    }

    companion object {
        /** About 2.7 g: a firm wrist shake, well above walking or putting the phone down. */
        const val DEFAULT_THRESHOLD_G = 2.7f
        const val STANDARD_GRAVITY = 9.80665f
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val NONE = Long.MIN_VALUE
    }
}
