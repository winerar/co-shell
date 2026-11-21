package app.coshell.ai

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger

/**
 * The add-on's only entry point (#587): a bound service the co-shell app talks to with [AddonProtocol] messages. Requests run on
 * one worker thread (the model does one thing at a time); the download runs on its own thread so a status question never waits.
 */
class AddonService : Service() {
    private lateinit var worker: HandlerThread
    private lateinit var handler: Handler
    private lateinit var model: ModelFile
    private val llm by lazy { Llm(applicationContext) }
    @Volatile private var downloading = false
    @Volatile private var cancelDownload = false
    private val idleCheck = object : Runnable {
        override fun run() {
            llm.closeIfIdle()
            handler.postDelayed(this, Llm.IDLE_MS / 4)
        }
    }

    override fun onCreate() {
        model = ModelFile(applicationContext)
        worker = HandlerThread("coshell-ai").also { it.start() }
        handler = Handler(worker.looper) { message -> handle(message); true }
        handler.postDelayed(idleCheck, Llm.IDLE_MS / 4)
    }

    private val messenger by lazy { Messenger(Handler(Looper.getMainLooper()) { message -> dispatch(message); true }) }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    override fun onDestroy() {
        cancelDownload = true
        handler.removeCallbacksAndMessages(null)
        worker.quitSafely()
        llm.close()
    }

    /** Light questions are answered at once; model work goes to the worker thread. */
    private fun dispatch(message: Message) {
        when (message.what) {
            AddonProtocol.STATUS -> reply(message, AddonProtocol.STATE, arg2 = model.state())
            AddonProtocol.CANCEL -> cancelDownload = true
            AddonProtocol.DELETE -> {
                cancelDownload = true
                handler.post { llm.close(); model.delete(); reply(message, AddonProtocol.STATE, arg2 = model.state()) }
            }
            AddonProtocol.DOWNLOAD -> startDownload(message)
            AddonProtocol.COMPLETE -> handler.sendMessage(Message.obtain(message))
        }
    }

    private fun handle(message: Message) {
        if (message.what != AddonProtocol.COMPLETE) return
        val data = message.data
        val text = runCatching {
            check(model.state() == AddonProtocol.MODEL_READY) { "model is not downloaded" }
            llm.complete(model.file, data.getString(AddonProtocol.KEY_SYSTEM).orEmpty(), data.getString(AddonProtocol.KEY_PROMPT).orEmpty())
        }
        val bundle = Bundle().apply {
            text.onSuccess { putString(AddonProtocol.KEY_TEXT, it) }.onFailure { putString(AddonProtocol.KEY_ERROR, it.message ?: it.javaClass.simpleName) }
        }
        reply(message, AddonProtocol.RESULT, data = bundle)
    }

    private fun startDownload(message: Message) {
        if (downloading) return
        downloading = true
        cancelDownload = false
        Thread {
            val error = runCatching {
                model.download(
                    onProgress = { done, total ->
                        reply(message, AddonProtocol.PROGRESS, data = Bundle().apply { putLong(AddonProtocol.KEY_DONE, done); putLong(AddonProtocol.KEY_TOTAL, total) })
                    },
                    cancelled = { cancelDownload },
                )
            }.exceptionOrNull()
            downloading = false
            reply(message, AddonProtocol.STATE, arg2 = model.state(), data = Bundle().apply { error?.let { putString(AddonProtocol.KEY_ERROR, it.message ?: it.javaClass.simpleName) } })
        }.start()
    }

    private fun reply(request: Message, what: Int, arg2: Int = 0, data: Bundle = Bundle()) {
        val to = request.replyTo ?: return
        val answer = Message.obtain(null, what, request.arg1, arg2).also { it.data = data }
        runCatching { to.send(answer) } // the app may have gone away: nothing to tell
    }
}
