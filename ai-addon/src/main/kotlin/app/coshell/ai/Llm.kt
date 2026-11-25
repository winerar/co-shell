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
 * The LiteRT-LM engine: loaded on the first request or on [warm], closed after [IDLE_MS] without one (a request takes seconds, the load ~5 s).
 * Runs on the phone's GPU when it can (#583 «оболочка» п. 8: many times faster to read the prompt), else on the CPU — see [GpuChoice].
 * All calls come from one worker thread of [AddonService], so there is no locking here.
 */
@OptIn(ExperimentalApi::class)
class Llm(private val context: Context) {
    private var engine: Engine? = null
    private var lastUsed = 0L
    private val gpu = GpuChoice(File(context.filesDir, "gpu_state"))

    /** Loads the engine for [model] now (the person is about to ask), so the next [complete] does not wait for it. */
    fun warm(model: File) {
        engine(model)
        lastUsed = System.currentTimeMillis()
    }

    /** The model's answer to [prompt] under [system], thinking off, greedy decoding (the same settings as the benchmark). */
    fun complete(model: File, system: String, prompt: String): String {
        try {
            return runCatching { ask(engine(model), system, prompt) }.getOrElse { error ->
                // A GPU that loaded but fails to generate: remember it, answer on the CPU this time and from now on.
                if (!onGpu) throw error
                gpu.failed()
                close()
                ask(engine(model), system, prompt)
            }
        } finally {
            lastUsed = System.currentTimeMillis()
        }
    }

    private var onGpu = false

    private fun engine(model: File): Engine {
        engine?.let { return it }
        if (gpu.worthTrying()) {
            gpu.trying()
            val loaded = runCatching { load(model, Backend.GPU()) }.getOrNull()
            if (loaded != null) {
                gpu.worked()
                onGpu = true
                return loaded.also { engine = it }
            }
            gpu.failed()
        }
        onGpu = false
        return load(model, Backend.CPU()).also { engine = it }
    }

    private fun load(model: File, backend: Backend): Engine =
        Engine(EngineConfig(model.path, backend, cacheDir = context.cacheDir.path, maxNumTokens = 2048)).also { it.initialize() }

    private fun ask(e: Engine, system: String, prompt: String): String {
        val config = ConversationConfig(
            systemInstruction = Contents.of(system),
            samplerConfig = SamplerConfig(1, 1.0, 0.0, 0),
            thinkingConfig = ThinkingConfig(false, 0),
        )
        e.createConversation(config).use { conversation ->
            // Thinking is off in the config; the Qwen-only «/no_think» tag is not sent to Gemma (#587 tuning: it was noise in the prompt).
            val message = conversation.sendMessage(prompt)
            return message.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
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

/**
 * Whether to try the GPU, remembered in a small file across launches. The GPU is tried until it fails once; «trying» is written before
 * the native load, so a load that kills the process (a driver crash is not an exception) is seen as a failure on the next launch.
 */
class GpuChoice(private val file: File) {
    fun worthTrying(): Boolean = when (runCatching { file.readText() }.getOrNull()) {
        FAILED, TRYING -> false
        else -> true
    }

    fun trying() = write(TRYING)
    fun worked() = write(OK)
    fun failed() = write(FAILED)

    private fun write(state: String) { runCatching { file.writeText(state) } }

    private companion object {
        const val TRYING = "trying"
        const val OK = "ok"
        const val FAILED = "failed"
    }
}
