package app.coshell.crash

import kotlin.time.Clock

/**
 * Catches crashes nobody handled (#218): saves them to [log] and hands them on to the system handler,
 * so Android still closes the app as usual. Install once, first thing in `Application.onCreate`.
 */
class CrashRecorder(
    private val log: CrashLog,
    private val clock: Clock,
    private val versionCode: Int,
) : Thread.UncaughtExceptionHandler {

    private var next: Thread.UncaughtExceptionHandler? = null

    fun install() {
        next = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        runCatching { log.save(crashText(error, clock.now(), versionCode, thread.name)) }
        next?.uncaughtException(thread, error)
    }
}
