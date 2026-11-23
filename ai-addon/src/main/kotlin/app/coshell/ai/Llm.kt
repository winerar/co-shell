package app.coshell.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File

/**
 * The LiteRT-LM engine: loaded on the first request, closed after [IDLE_MS] without one (a request takes seconds, the load ~5 s).
 * All calls come from one worker thread of [AddonService], so there is no locking here.
 */
@OptIn(ExperimentalApi::class)
class Llm(private val context: Context) {
    private var engine: Engine? = null
    private var lastUsed = 0L

    /** The model's answer to [prompt] under [system], thinking off, greedy decoding (the same settings as the benchmark). */
    fun complete(model: File, system: String, prompt: String): String {
        val e = engine ?: Engine(EngineConfig(model.path, Backend.CPU(), cacheDir = context.cacheDir.path, maxNumTokens = 2048))
            .also { it.initialize(); engine = it }
        val config = ConversationConfig(
            systemInstruction = Contents.of(system),
            samplerConfig = SamplerConfig(1, 1.0, 0.0, 0),
            thinkingConfig = ThinkingConfig(false, 0),
        )
        try {
            e.createConversation(config).use { conversation ->
                // Thinking is off in the config; the Qwen-only «/no_think» tag is not sent to Gemma (#587 tuning: it was noise in the prompt).
                val message = conversation.sendMessage(prompt)
                return message.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
            }
        } finally {
            lastUsed = System.currentTimeMillis()
        }
    }

    /** Called by the service's timer: closes an engine that has been idle long enough. */
    fun closeIfIdle(now: Long = System.currentTimeMillis()) {
        if (engine != null && now - lastUsed >= IDLE_MS) close()
    }

    fun close() {
        runCatching { engine?.close() }
        engine = null
    }

    companion object {
        const val IDLE_MS = 60_000L
    }
}
