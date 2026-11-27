package app.coshell.core.domain.settings

/**
 * Time of day for reminders as minutes since midnight (#777: «Другое…» with a counter). The counter steps by [STEP] and wraps
 * around midnight; [hour]/[minute] split it for the scheduler and for «9:30» on screen.
 */
object ClockMinutes {
    const val STEP = 15
    const val DAY = 24 * 60

    fun of(hour: Int, minute: Int = 0): Int = hour * 60 + minute

    fun isValid(minutes: Int): Boolean = minutes in 0 until DAY

    fun hour(minutes: Int): Int = minutes / 60

    fun minute(minutes: Int): Int = minutes % 60

    /** [steps] counter presses away from [minutes] (negative — back), snapped to the [STEP] grid, across midnight. */
    fun step(minutes: Int, steps: Int): Int {
        val snapped = minutes - minutes.mod(STEP)
        val base = if (snapped != minutes && steps < 0) snapped + STEP else snapped
        return (base + steps * STEP).mod(DAY)
    }
}
