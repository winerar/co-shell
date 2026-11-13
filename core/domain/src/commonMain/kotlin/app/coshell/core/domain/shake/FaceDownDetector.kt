package app.coshell.core.domain.shake

/**
 * Tells when the phone lies screen down (#324: sums hide on a café table). Pure logic over the same accelerometer
 * stream as [ShakeDetector].
 *
 * Screen down means gravity points out of the screen: Z ≈ −9.8 m/s² and little tilt. It has to stay so for
 * [settleMillis] (a phone flipped over in the hand passes through that pose for a moment). Lifting the phone
 * (Z above [liftZ]) shows the sums again after [liftMillis] — fast, the person wants to see them.
 */
class FaceDownDetector(
    private val downZ: Float = -8.5f,
    private val maxTilt: Float = 3f,
    private val liftZ: Float = -5f,
    private val settleMillis: Long = 600,
    private val liftMillis: Long = 150,
) {
    /** Current answer: the sums should be hidden. */
    var isFaceDown: Boolean = false
        private set

    private var changingSince = NONE

    /**
     * Takes one reading (m/s² on each axis, time in nanoseconds of any monotonic clock).
     * Returns `true` exactly when [isFaceDown] changed on this sample.
     */
    fun onSample(x: Float, y: Float, z: Float, timestampNanos: Long): Boolean {
        val now = timestampNanos / NANOS_PER_MILLI
        val pointsDown = z <= downZ && x * x + y * y <= maxTilt * maxTilt
        val wantsChange = if (isFaceDown) z > liftZ else pointsDown
        if (!wantsChange) {
            changingSince = NONE
            return false
        }
        if (changingSince == NONE) changingSince = now
        val needed = if (isFaceDown) liftMillis else settleMillis
        if (now - changingSince < needed) return false
        isFaceDown = !isFaceDown
        changingSince = NONE
        return true
    }

    /** Back to «face up», e.g. when the screen stops listening (app in background). */
    fun reset() {
        isFaceDown = false
        changingSince = NONE
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
        const val NONE = Long.MIN_VALUE
    }
}
