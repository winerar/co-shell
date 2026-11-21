package app.coshell.ai

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException

/** Bytes of the model downloaded so far; [total] is `0` until the server says how big the file is. */
data class ModelProgress(val done: Long, val total: Long)

/** What the app can ask the installed add-on to do about its model (#587). */
interface AddonControl {
    suspend fun status(): AddonStatus

    /** Emits progress until the model is on the phone (then completes) or stops (cancelled — completes too; check [status]); throws [IOException] on failure. */
    fun downloadModel(): Flow<ModelProgress>

    suspend fun cancelDownload()
    suspend fun deleteModel()
}

internal class BoundAddonControl(private val binder: AddonBinder) : AddonControl {
    constructor(context: Context) : this(AddonBinder(context))

    override suspend fun status(): AddonStatus = binder.status()

    override fun downloadModel(): Flow<ModelProgress> = callbackFlow {
        // Not exclusive: «Отменить» and status questions must get through while the download talks.
        val finished = binder.converse(AddonProtocol.DOWNLOAD, Bundle(), DOWNLOAD_IDLE_MS, exclusive = false) { reply ->
            when (reply.what) {
                AddonProtocol.PROGRESS -> { trySend(ModelProgress(reply.data.getLong(AddonProtocol.KEY_DONE), reply.data.getLong(AddonProtocol.KEY_TOTAL))); false }
                AddonProtocol.STATE -> {
                    reply.data.getString(AddonProtocol.KEY_ERROR)?.let { close(IOException(it)) }
                    true
                }
                else -> false
            }
        }
        if (finished) close() else close(IOException("The add-on does not answer"))
        awaitClose()
    }

    override suspend fun cancelDownload() {
        binder.converse(AddonProtocol.CANCEL, Bundle(), QUICK_MS, exclusive = false) { true }
    }

    override suspend fun deleteModel() {
        binder.converse(AddonProtocol.DELETE, Bundle(), QUICK_MS * 4) { it.what == AddonProtocol.STATE }
    }

    private companion object {
        const val DOWNLOAD_IDLE_MS = 90_000L // progress arrives twice a second; a minute of silence means a dead connection
        const val QUICK_MS = 1_500L
    }
}
