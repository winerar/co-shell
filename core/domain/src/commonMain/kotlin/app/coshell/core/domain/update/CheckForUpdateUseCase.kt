package app.coshell.core.domain.update

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/** Outcome of [CheckForUpdateUseCase]. */
sealed interface UpdateCheckResult {
    data class Available(val release: AppRelease) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    /** Automatic check skipped (the last one was less than [CheckForUpdateUseCase.interval] ago), nothing newer known. */
    data object CheckedRecently : UpdateCheckResult
    /** This build has no update server configured (local builds). */
    data object NotConfigured : UpdateCheckResult
    data class Failed(val reason: String) : UpdateCheckResult
}

/**
 * Asks the [ReleaseSource] whether a newer build exists. Automatic checks (on app start) go to the
 * network at most once per [interval]; in between, the release found last time is reported again,
 * so "update available" is shown on every app start until it is installed.
 * A check started by the user ([force]) always runs.
 */
class CheckForUpdateUseCase(
    private val source: ReleaseSource,
    private val store: UpdateStateStore,
    private val installed: InstalledVersion,
    private val clock: Clock,
    val interval: Duration = 1.hours,
) {
    suspend operator fun invoke(force: Boolean = false): UpdateCheckResult {
        val now = clock.now()
        if (!force) {
            val last = store.lastCheckAt()
            if (last != null && now - last in Duration.ZERO..<interval) return knownOrRecent()
        }
        val release = try {
            source.latestRelease() ?: return UpdateCheckResult.NotConfigured
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return UpdateCheckResult.Failed(e.message ?: e::class.simpleName.orEmpty())
        }
        store.setLastCheckAt(now)
        store.setKnownRelease(release)
        return if (release.versionCode > installed.versionCode) {
            UpdateCheckResult.Available(release)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    private suspend fun knownOrRecent(): UpdateCheckResult {
        val known = store.knownRelease()
        return if (known != null && known.versionCode > installed.versionCode) {
            UpdateCheckResult.Available(known)
        } else {
            UpdateCheckResult.CheckedRecently
        }
    }
}
