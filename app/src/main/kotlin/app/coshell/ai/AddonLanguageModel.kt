package app.coshell.ai

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import app.coshell.core.domain.ai.LanguageModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicInteger

/** The add-on's answer to a status question: [modelState] is one of `AddonProtocol.MODEL_*`. */
class AddonStatus(val installed: Boolean, val modelState: Int) {
    val ready: Boolean get() = installed && modelState == AddonProtocol.MODEL_READY
}

/**
 * [LanguageModel] backed by the optional «ИИ на телефоне» add-on (#587, `ai-addon/`): binds to its service for each call and talks
 * through [AddonProtocol]. [available] is the last known state — call [refresh] on launch and after installing or downloading.
 * Without the add-on (or without a downloaded model) every call returns `null` and the app works by rules.
 */
class AddonLanguageModel(private val context: Context) : LanguageModel {
    @Volatile private var status = AddonStatus(installed = false, modelState = AddonProtocol.MODEL_MISSING)
    private val ids = AtomicInteger()
    private val lock = Mutex()

    override val available: Boolean get() = status.ready

    /** Asks the add-on for its state (and remembers it for [available]). */
    suspend fun refresh(): AddonStatus {
        val reply = call(AddonProtocol.STATUS, Bundle(), STATUS_TIMEOUT_MS)
        status = if (reply == null) AddonStatus(false, AddonProtocol.MODEL_MISSING) else AddonStatus(true, reply.arg2)
        return status
    }

    override suspend fun complete(system: String, prompt: String): String? {
        if (!available) return null
        val data = Bundle().apply { putString(AddonProtocol.KEY_SYSTEM, system); putString(AddonProtocol.KEY_PROMPT, prompt) }
        val reply = call(AddonProtocol.COMPLETE, data, COMPLETE_TIMEOUT_MS) ?: return null
        return reply.data.getString(AddonProtocol.KEY_TEXT)
    }

    /** One request/reply on a fresh binding; `null` when the add-on is missing, refuses us or does not answer in time. */
    private suspend fun call(what: Int, data: Bundle, timeoutMs: Long): Message? = lock.withLock {
        val reply = CompletableDeferred<Message?>()
        val id = ids.incrementAndGet()
        val handler = Handler(Looper.getMainLooper()) { message -> if (message.arg1 == id) reply.complete(Message.obtain(message)); true }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val request = Message.obtain(null, what, id, 0).also { it.data = data; it.replyTo = Messenger(handler) }
                runCatching { Messenger(service).send(request) }.onFailure { reply.complete(null) }
            }

            override fun onServiceDisconnected(name: ComponentName) { reply.complete(null) }
            override fun onBindingDied(name: ComponentName) { reply.complete(null) }
            override fun onNullBinding(name: ComponentName) { reply.complete(null) }
        }
        val intent = Intent().setClassName(AddonProtocol.PACKAGE, AddonProtocol.SERVICE)
        val bound = runCatching { context.bindService(intent, connection, Context.BIND_AUTO_CREATE) }.getOrDefault(false)
        if (!bound) {
            runCatching { context.unbindService(connection) }
            return null
        }
        try {
            withTimeout(timeoutMs) { reply.await() }
        } catch (_: TimeoutCancellationException) {
            null
        } finally {
            runCatching { context.unbindService(connection) }
        }
    }

    private companion object {
        const val STATUS_TIMEOUT_MS = 5_000L
        const val COMPLETE_TIMEOUT_MS = 120_000L
    }
}
