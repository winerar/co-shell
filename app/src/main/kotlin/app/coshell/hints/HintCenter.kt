package app.coshell.hints

import app.coshell.R
import java.util.TimeZone

import androidx.annotation.StringRes
import app.coshell.core.domain.hints.HintId
import app.coshell.core.domain.hints.HintKind
import app.coshell.core.domain.hints.HintPlanner
import app.coshell.core.domain.hints.HintStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * What is on the banner over the bottom bar: a gesture card ([HintKind.GESTURE]) or a warm pill (moments and plain
 * notices with [id] = null, like «Подсказки включены»).
 */
data class HintBanner(val id: HintId?, @StringRes val text: Int, val args: List<String> = emptyList())

/** Screens ask «may I show this hint now?» through this; the app provides it via [LocalHintHost]. */
interface HintHost {
    /** The banner now on screen (gesture or moment), null when none. */
    val banner: StateFlow<HintBanner?>

    /** Word hints: true if [id] may be shown now (and is counted as shown). */
    suspend fun claim(id: HintId): Boolean

    /** Gestures and moments: picks the first of [texts] by priority that may show now and puts it on the banner. */
    suspend fun offer(texts: Map<HintId, Int>, args: Map<HintId, List<String>> = emptyMap()): Boolean

    /** Words shown by a sheet itself: the first of [ids] by priority that may show now, counted as shown; null when none. */
    suspend fun claimFirst(ids: Set<HintId>): HintId?

    /** A plain notice without any limits (the reset answer). */
    fun say(@StringRes text: Int)

    fun dismiss()

    /** True from «month_end» until «Отчёты» is opened: the dot on that tab. */
    val reportsDot: StateFlow<Boolean>

    /** «Отчёты» was opened: the dot goes away for good. */
    suspend fun reportsOpened()
}

/**
 * Decides, with [HintPlanner], whether a hint may appear: hints on, not shown before, one word/gesture per visit
 * (= app process) and per day; warm moments are outside that limit. A claimed hint counts as shown at once, so
 * «✕» only closes it (`docs/agents/hints.md`).
 */
@Singleton
class HintCenter(private val store: HintStore, private val today: () -> Long) : HintHost {
    @Inject constructor(store: HintStore) : this(store, { localDay(System.currentTimeMillis()) })

    private val lock = Mutex()
    private var shownThisVisit = false
    private var launchMomentsTaken = false
    private val _banner = MutableStateFlow<HintBanner?>(null)
    override val banner: StateFlow<HintBanner?> = _banner.asStateFlow()

    /** Claimed while another banner was up: shown one by one after it, so a claimed moment is never lost. */
    private val queue = ArrayDeque<HintBanner>()

    // One state for every screen («Обзор» and «Настройки» have their own view models): null until read from the store.
    private val _enabled = MutableStateFlow<Boolean?>(null)
    private val _stepsHidden = MutableStateFlow<Boolean?>(null)

    /** «Подсказки» on/off; null while loading (nothing is shown then). */
    val enabled: StateFlow<Boolean?> = _enabled.asStateFlow()

    /** «Скрыть шаги» was pressed; null while loading. */
    val stepsHidden: StateFlow<Boolean?> = _stepsHidden.asStateFlow()

    suspend fun load() {
        if (_enabled.value == null) _enabled.value = store.enabled()
        if (_stepsHidden.value == null) _stepsHidden.value = store.stepsHidden()
    }

    suspend fun setEnabled(value: Boolean) {
        _enabled.value = value
        store.setEnabled(value)
        if (!value) clearBanners()
    }

    suspend fun hideSteps() {
        _stepsHidden.value = true
        store.setStepsHidden(true)
    }

    /** «Показать подсказки заново»: nothing is shown yet, the steps are back, hints are on; answered with a notice. */
    suspend fun reset() {
        store.reset()
        _enabled.value = true
        _stepsHidden.value = false
        resetVisit()
        clearBanners()
        say(R.string.hints_reset_done)
    }

    /** True once per process: the time-based moments are checked a single time per launch, whoever asks. */
    fun takeLaunchMoments(): Boolean = synchronized(this) { !launchMomentsTaken.also { launchMomentsTaken = true } }
    private val _reportsDot = MutableStateFlow(false)
    override val reportsDot: StateFlow<Boolean> = _reportsDot.asStateFlow()

    /** At launch: the dot of an earlier «month_end» that «Отчёты» has not answered yet. */
    suspend fun restoreDot() {
        val shown = store.shown()
        _reportsDot.value = HintId.MONTH_END.key in shown && DOT_SEEN_KEY !in shown
    }

    override suspend fun reportsOpened() {
        if (!_reportsDot.value) return
        _reportsDot.value = false
        store.markShown(DOT_SEEN_KEY)
    }

    override suspend fun claim(id: HintId): Boolean = claimFirst(setOf(id)) != null

    override suspend fun offer(texts: Map<HintId, Int>, args: Map<HintId, List<String>>): Boolean {
        val id = claimFirst(texts.keys) ?: return false
        show(HintBanner(id, texts.getValue(id), args[id].orEmpty()))
        if (id == HintId.MONTH_END) _reportsDot.value = true
        return true
    }

    override fun say(text: Int) {
        show(HintBanner(null, text))
    }

    override fun dismiss() {
        synchronized(this) { _banner.value = queue.removeFirstOrNull() }
    }

    private fun show(banner: HintBanner) = synchronized(this) {
        if (_banner.value == null) _banner.value = banner else queue.addLast(banner)
    }

    private fun clearBanners() = synchronized(this) {
        queue.clear()
        _banner.value = null
    }

    /** «Показать подсказки заново»: a new visit as far as the limits go. */
    fun resetVisit() {
        shownThisVisit = false
        _reportsDot.value = false
    }

    override suspend fun claimFirst(ids: Set<HintId>): HintId? = lock.withLock {
        val candidates = ids
        val day = today()
        val pick = HintPlanner.next(candidates, store.enabled(), store.shown(), store.lastShownDay(), day, shownThisVisit)
            ?: return null
        store.markShown(pick.key)
        if (pick.kind != HintKind.MOMENT) {
            store.setLastShownDay(day)
            shownThisVisit = true
        }
        pick
    }

    private companion object {
        /** «Already shown» key of the Reports dot; not a [HintId], `reset()` clears it with the rest. */
        const val DOT_SEEN_KEY = "reports_dot_seen"
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

/** The day number of [millis] on the phone's clock (not UTC: in Argentina a UTC day turns at 21:00). */
fun localDay(millis: Long): Long = (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_PER_DAY

