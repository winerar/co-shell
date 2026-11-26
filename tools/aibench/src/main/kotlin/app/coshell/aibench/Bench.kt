package app.coshell.aibench

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Debug
import app.coshell.aieval.AiEval
import app.coshell.aieval.EvalGold
import app.coshell.aieval.RecordingModel
import app.coshell.core.domain.ai.LanguageModel
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File
import kotlinx.coroutines.runBlocking

/**
 * Runs the gold set through **the app's own code** (#742, `tools/ai-eval`: rules, [app.coshell.core.domain.ai.NoteHints], ReceiptAi,
 * PayeeCategoryAi) with one model on one backend, and measures speed, memory and heat. Every request and answer is recorded
 * ([Report.answersJson]) — saved into `docs/samples/ai/answers/` it lets the JVM tests replay the run without a phone.
 */
@OptIn(ExperimentalApi::class)
class Bench(private val ctx: Context) {
    class Report(
        val model: String, val backend: String, val loadSec: Double, val tokPerSec: Double, val peakPssMb: Long, val tempBefore: Double,
        val tempAfter: Double, val totalSec: Double, val scores: List<AiEval.Score>, val rulesOnly: List<AiEval.Score>, val answersJson: String,
    )

    @Volatile private var peakPss = 0L
    private var sec = 0.0
    private var tokens = 0

    private fun gold(): EvalGold {
        fun asset(path: String) = ctx.assets.open(path).bufferedReader().use { it.readText() }
        // `file` entries are relative to docs/samples/ai/ («../receipts/x.txt»); the assets root is docs/samples/.
        return EvalGold(asset("ai/eval.json")) { asset(it.removePrefix("../")) }
    }

    fun run(c: Candidate, file: File, gpu: Boolean, log: (String) -> Unit, cancelled: () -> Boolean): Report {
        peakPss = 0; sec = 0.0; tokens = 0
        val sampler = Thread {
            while (!Thread.currentThread().isInterrupted) {
                peakPss = maxOf(peakPss, Debug.getPss() / 1024)
                try { Thread.sleep(400) } catch (_: InterruptedException) { return@Thread }
            }
        }.also { it.start() }
        ExperimentalFlags.enableBenchmark = true
        val gold = gold()
        val tempBefore = batteryTemp()
        val started = System.nanoTime()
        try {
            val rulesOnly = runBlocking { AiEval(gold, null).all() }
            val t0 = System.nanoTime()
            val engine = Engine(EngineConfig(file.path, if (gpu) Backend.GPU() else Backend.CPU(), cacheDir = ctx.cacheDir.path, maxNumTokens = 2048))
            engine.initialize()
            val loadSec = (System.nanoTime() - t0) / 1e9
            log("модель загружена за %.1f с".format(loadSec))
            engine.use { e ->
                val recording = RecordingModel(EngineModel(e, c, cancelled))
                val eval = AiEval(gold, recording)
                val scores = mutableListOf<AiEval.Score>()
                runBlocking {
                    for (step in listOf<suspend () -> AiEval.Score>({ eval.notes(false) }, { eval.notes(true) }, { eval.payees(false) }, { eval.payees(true) }, { eval.receipts() })) {
                        if (cancelled()) break
                        val s = step()
                        scores += s
                        log("${s.line} · запросов: ${s.asked}")
                        s.misses.forEach { log("  ✗ $it") }
                    }
                }
                val label = "${c.label} [${if (gpu) "GPU" else "CPU"}]"
                return Report(
                    c.label, if (gpu) "GPU" else "CPU", loadSec, if (sec > 0) tokens / sec else 0.0, peakPss, tempBefore, batteryTemp(),
                    (System.nanoTime() - started) / 1e9, scores, rulesOnly, recording.toJson(label, scores.map { it.short }),
                )
            }
        } finally {
            sampler.interrupt()
        }
    }

    private fun batteryTemp(): Double {
        val i: Intent? = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return (i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
    }

    /** The engine as the app's [LanguageModel]: one conversation per request with the app's system text, greedy decoding, thinking off. */
    private inner class EngineModel(private val engine: Engine, private val c: Candidate, private val cancelled: () -> Boolean) : LanguageModel {
        override val available = true

        override suspend fun complete(system: String, prompt: String): String? {
            if (cancelled()) return null
            val cfg = ConversationConfig(
                systemInstruction = Contents.of(system),
                samplerConfig = SamplerConfig(1, 1.0, 0.0, 0),
                thinkingConfig = ThinkingConfig(!c.thinkingOff, 0),
            )
            return runCatching {
                engine.createConversation(cfg).use { conv ->
                    val t = System.nanoTime()
                    val message = conv.sendMessage(prompt)
                    sec += (System.nanoTime() - t) / 1e9
                    val text = message.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
                    val decoded = runCatching { conv.getBenchmarkInfo().lastDecodeTokenCount }.getOrDefault(0)
                    tokens += if (decoded > 0) decoded else (text.length / 3).coerceAtLeast(1)
                    text
                }
            }.getOrNull()
        }
    }
}
