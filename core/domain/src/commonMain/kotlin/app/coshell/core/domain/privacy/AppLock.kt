package app.coshell.core.domain.privacy

/**
 * Is the app locked right now (#145)? Pure state over three events: the app went to the background, came back, the person
 * unlocked it. The app starts locked when a PIN is set; coming back locks it again after [RelockDelay].
 * Times — milliseconds of any monotonic clock (the app passes `elapsedRealtime`).
 */
class AppLock {
    var locked: Boolean = true
        private set

    private var leftAt: Long? = null

    /** Options were read or changed: no PIN — never locked. */
    fun onOptions(options: PrivacyOptions) {
        if (!options.pinEnabled) locked = false
    }

    fun onBackground(now: Long) {
        if (leftAt == null) leftAt = now
    }

    /** Back on screen: locks again when the app was away at least [RelockDelay.millis]. */
    fun onForeground(now: Long, options: PrivacyOptions) {
        val left = leftAt
        leftAt = null
        if (!options.pinEnabled) {
            locked = false
            return
        }
        if (left != null && now - left >= options.relock.millis) locked = true
    }

    fun unlock() {
        locked = false
    }
}
