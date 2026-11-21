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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicInteger

/**
 * One conversation with the add-on's service (#587): binds, sends a request, hands every reply to a callback until it says «done»,
 * unbinds. Everything that talks to the add-on goes through here, so binding, timeouts and unbinding live in one place.
 */
internal class AddonBinder(private val context: Context) {
    private val ids = AtomicInteger()
    private val lock = Mutex() // the service answers one thing at a time; a status question must not queue behind a model request

    /**
     * Sends [what] with [data] and passes each reply to [onReply] (`true` = the conversation is over). Gives up when no reply
     * comes for [idleTimeoutMs]. Returns `true` when [onReply] ended it; `false` when the add-on is missing, refuses us or went quiet.
     */
    suspend fun converse(what: Int, data: Bundle, idleTimeoutMs: Long, exclusive: Boolean = true, onReply: (Message) -> Boolean): Boolean {
        suspend fun run(): Boolean {
            val replies = Channel<Message?>(Channel.UNLIMITED)
            val id = ids.incrementAndGet()
            val handler = Handler(Looper.getMainLooper()) { message -> if (message.arg1 == id) replies.trySend(Message.obtain(message)); true }
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val request = Message.obtain(null, what, id, 0).also { it.data = data; it.replyTo = Messenger(handler) }
                    runCatching { Messenger(service).send(request) }.onFailure { replies.trySend(null) }
                }

                override fun onServiceDisconnected(name: ComponentName) { replies.trySend(null) }
                override fun onBindingDied(name: ComponentName) { replies.trySend(null) }
                override fun onNullBinding(name: ComponentName) { replies.trySend(null) }
            }
            val intent = Intent().setClassName(AddonProtocol.PACKAGE, AddonProtocol.SERVICE)
            val bound = runCatching { context.bindService(intent, connection, Context.BIND_AUTO_CREATE) }.getOrDefault(false)
            if (!bound) {
                runCatching { context.unbindService(connection) }
                return false
            }
            try {
                while (true) {
                    val reply = withTimeout(idleTimeoutMs) { replies.receive() } ?: return false
                    if (onReply(reply)) return true
                }
            } catch (_: TimeoutCancellationException) {
                return false
            } finally {
                runCatching { context.unbindService(connection) }
            }
        }
        return if (exclusive) lock.withLock { run() } else run()
    }
}
