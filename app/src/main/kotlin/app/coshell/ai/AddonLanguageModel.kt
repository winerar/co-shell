package app.coshell.ai

import android.content.Context
import android.os.Bundle
import app.coshell.core.domain.ai.LanguageModel

/** The add-on's answer to a status question: [modelState] is one of `AddonProtocol.MODEL_*`. */
class AddonStatus(val installed: Boolean, val modelState: Int) {
    val ready: Boolean get() = installed && modelState == AddonProtocol.MODEL_READY
}

/**
 * [LanguageModel] backed by the optional «ИИ на телефоне» add-on (#587, `ai-addon/`): talks to its service through [AddonBinder].
 * [available] is the last known state — call [refresh] on launch and after installing or downloading.
 * Without the add-on (or without a downloaded model) every call returns `null` and the app works by rules.
 */
class AddonLanguageModel internal constructor(private val binder: AddonBinder) : LanguageModel {
    constructor(context: Context) : this(AddonBinder(context))

    @Volatile private var status = AddonStatus(installed = false, modelState = AddonProtocol.MODEL_MISSING)

    override val available: Boolean get() = status.ready

    /** Asks the add-on for its state (and remembers it for [available]). */
    suspend fun refresh(): AddonStatus = binder.status().also { status = it }

    override suspend fun complete(system: String, prompt: String): String? {
        if (!available) return null
        val data = Bundle().apply { putString(AddonProtocol.KEY_SYSTEM, system); putString(AddonProtocol.KEY_PROMPT, prompt) }
        var text: String? = null
        binder.converse(AddonProtocol.COMPLETE, data, COMPLETE_TIMEOUT_MS) { reply ->
            if (reply.what == AddonProtocol.RESULT) { text = reply.data.getString(AddonProtocol.KEY_TEXT); true } else false
        }
        return text
    }

    private companion object {
        const val COMPLETE_TIMEOUT_MS = 120_000L
    }
}

/** One status question on a fresh binding: `installed = false` when the add-on is missing or does not answer. */
internal suspend fun AddonBinder.status(): AddonStatus {
    var state = -1
    converse(AddonProtocol.STATUS, Bundle(), STATUS_TIMEOUT_MS) { reply ->
        if (reply.what == AddonProtocol.STATE) { state = reply.arg2; true } else false
    }
    return if (state < 0) AddonStatus(false, AddonProtocol.MODEL_MISSING) else AddonStatus(true, state)
}

private const val STATUS_TIMEOUT_MS = 5_000L
