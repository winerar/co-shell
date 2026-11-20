package app.coshell.privacy

import app.coshell.core.domain.privacy.AppLock
import app.coshell.core.domain.privacy.PinHasher
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.privacy.PrivacySettings
import app.coshell.core.domain.privacy.matches
import app.coshell.core.domain.privacy.store
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

/** Answer to a typed PIN. */
sealed interface PinCheck {
    data object Ok : PinCheck
    /** Wrong; [waitMillis] > 0 — the next attempt only after that. */
    data class Wrong(val waitMillis: Long) : PinCheck
    /** Still waiting after earlier wrong attempts; the PIN was not checked. */
    data class Wait(val waitMillis: Long) : PinCheck
}

/**
 * The app-wide privacy state (#145): locked or not ([AppLock]), sums hidden or not («глаз», «экраном вниз», «скрывать при
 * запуске»). MainActivity feeds it the background/foreground moments; the lock screen, «Обзор» and the settings read it.
 * [clock] — epoch ms (PIN waits survive a restart), [monotonic] — elapsed ms (the relock delay ignores clock changes).
 */
@Singleton
class PrivacyCenter internal constructor(
    private val settings: PrivacySettings,
    private val hasher: PinHasher,
    private val clock: () -> Long,
    private val monotonic: () -> Long,
    private val work: CoroutineContext,
    scope: CoroutineScope,
) {
    @Inject constructor(settings: PrivacySettings, hasher: PinHasher) : this(
        settings, hasher, System::currentTimeMillis, { android.os.SystemClock.elapsedRealtime() }, Dispatchers.Default,
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    )

    private val lock = AppLock()
    private var leftAt: Long? = null
    private var hiddenSet = false

    private val lockedState = MutableStateFlow(true)
    /** The lock screen covers the app. `true` until the options are read, so nothing flashes before the PIN. */
    val locked: StateFlow<Boolean> = lockedState.asStateFlow()

    private val hiddenState = MutableStateFlow(false)
    val hidden: StateFlow<Boolean> = hiddenState.asStateFlow()

    private val faceDownState = MutableStateFlow(false)
    /** The sums were just hidden by laying the phone down: a short pill says how to show them. */
    val faceDownNotice: StateFlow<Boolean> = faceDownState.asStateFlow()

    /** `null` until read from the device. */
    val options: StateFlow<PrivacyOptions?> = settings.options
        .onEach { o ->
            lock.onOptions(o)
            lockedState.value = lock.locked
            if (!hiddenSet) {
                hiddenSet = true
                hiddenState.value = o.hideOnLaunch
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun onBackground() {
        lock.onBackground(monotonic())
        if (leftAt == null) leftAt = monotonic()
    }

    fun onForeground() {
        val o = options.value ?: return
        val now = monotonic()
        val away = leftAt?.let { now - it }
        leftAt = null
        lock.onForeground(now, o)
        lockedState.value = lock.locked
        // A new visit (away at least the relock delay) opens hidden again when asked to.
        if (o.hideOnLaunch && away != null && away >= o.relock.millis) hiddenState.value = true
    }

    fun toggleHidden() {
        hiddenState.value = !hiddenState.value
        faceDownState.value = false
    }

    /** The phone lay screen down: the sums hide and stay hidden until the «глаз». */
    fun onFaceDown() {
        if (options.value?.hideFaceDown != true || hiddenState.value) return
        hiddenState.value = true
        faceDownState.value = true
    }

    fun dismissFaceDownNotice() {
        faceDownState.value = false
    }

    suspend fun check(pin: String): PinCheck {
        val now = clock()
        val attempts = settings.attempts()
        if (attempts.waitLeft(now) > 0) return PinCheck.Wait(attempts.waitLeft(now))
        val stored = settings.pin() ?: return PinCheck.Ok
        val ok = withContext(work) { stored.matches(pin, hasher) }
        if (ok) {
            settings.setAttempts(attempts.copy(failed = 0, blockedUntil = 0))
            return PinCheck.Ok
        }
        val next = attempts.fail(now)
        settings.setAttempts(next)
        return PinCheck.Wrong(next.waitLeft(now))
    }

    /** Right PIN, fingerprint or the phone's own lock. */
    fun unlock() {
        lock.unlock()
        lockedState.value = false
    }

    suspend fun setPin(pin: String) {
        val stored = withContext(work) { hasher.store(pin) }
        settings.setPin(stored)
        unlock()
    }

    suspend fun removePin() = settings.setPin(null)

    suspend fun change(change: (PrivacyOptions) -> PrivacyOptions) = settings.update(change)
}
