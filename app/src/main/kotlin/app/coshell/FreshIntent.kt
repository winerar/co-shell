package app.coshell

import android.content.Intent

/**
 * Is the activity's intent a new request (a share, a receipt link, a reminder's button)? Not when the activity is
 * [restored] after the process was killed, nor when opened from «Недавние»: Android hands back the original intent then.
 */
internal fun isFreshIntent(restored: Boolean, flags: Int): Boolean =
    !restored && flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0
