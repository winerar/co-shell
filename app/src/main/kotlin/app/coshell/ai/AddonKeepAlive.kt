package app.coshell.ai

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Keeps the add-on's service alive between requests (#583 «оболочка» п. 7). Every [AddonBinder] conversation binds and unbinds, and a
 * service nobody is bound to is destroyed — together with the loaded model, so each request paid the ~5 s load again. [hold] binds once
 * and lets go [KEEP_MS] after the last call; the add-on itself unloads an idle engine after its own minute (`Llm.IDLE_MS`).
 * All calls on the main thread (the timer is a main-looper [Handler]).
 */
internal class AddonKeepAlive(
    private val bind: (ServiceConnection) -> Boolean,
    private val unbind: (ServiceConnection) -> Unit,
    private val handler: Handler = Handler(Looper.getMainLooper()),
) {
    constructor(context: Context) : this(
        bind = { connection ->
            val intent = Intent().setClassName(AddonProtocol.PACKAGE, AddonProtocol.SERVICE)
            runCatching { context.bindService(intent, connection, Context.BIND_AUTO_CREATE) }.getOrDefault(false)
        },
        unbind = { connection -> runCatching { context.unbindService(connection) } },
    )

    /** `true` while the binding is held. */
    var held = false
        private set

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) = Unit
        override fun onServiceDisconnected(name: ComponentName) = Unit // the system rebinds by itself while we hold it
    }

    private val release = Runnable { if (held) { held = false; unbind(connection) } }

    /** Bind (if not yet) and keep the binding for [KEEP_MS] from now. A missing add-on simply does not bind. */
    fun hold() {
        handler.removeCallbacks(release)
        if (!held) {
            held = bind(connection)
            if (!held) { unbind(connection); return }
        }
        handler.postDelayed(release, KEEP_MS)
    }

    companion object {
        /** A bit longer than the add-on's own idle minute: the engine goes first, then the process may go. */
        const val KEEP_MS = 90_000L
    }
}
