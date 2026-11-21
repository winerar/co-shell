package app.coshell.aibench

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Debug
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File

/** Runs the gold set through one model and measures accuracy, speed, memory and heat — the prompts are those of `scripts/agent/ai_eval.py`. */
@OptIn(ExperimentalApi::class)
class Bench(private val ctx: Context, private val gold: Gold) {
    class Score(val name: String, val ok: Int, val of: Int, val note: String = "")
    class Report(
        val model: String, val backend: String, val loadSec: Double, val tokPerSec: Double, val peakPssMb: Long, val tempBefore: Double,
        val tempAfter: Double, val totalSec: Double, val scores: List<Score>,
    )

    @Volatile private var peakPss = 0L
    private var sec = 0.0
    private var tokens = 0

    fun run(c: Candidate, file: File, gpu: Boolean, quick: Boolean, log: (String) -> Unit, cancelled: () -> Boolean): Report {
        peakPss = 0; sec = 0.0; tokens = 0
        val sampler = Thread {
            while (!Thread.currentThread().isInterrupted) {
                peakPss = maxOf(peakPss, Debug.getPss() / 1024)
                try { Thread.sleep(400) } catch (_: InterruptedException) { return@Thread }
            }
        }.also { it.start() }
        ExperimentalFlags.enableBenchmark = true
        val tempBefore = batteryTemp()
        val started = System.nanoTime()
        try {
            val t0 = System.nanoTime()
            val engine = Engine(EngineConfig(file.path, if (gpu) Backend.GPU() else Backend.CPU(), cacheDir = ctx.cacheDir.path, maxNumTokens = 2048))
            engine.initialize()
            val loadSec = (System.nanoTime() - t0) / 1e9
            log("модель загружена за %.1f с".format(loadSec))
            val scores = mutableListOf<Score>()
            engine.use { e ->
                fun step(name: String, score: Score?) { if (score != null) { scores += score; log("$name: ${score.ok} из ${score.of} ${score.note}") } }
                step("категория", category(e, c, quick, log, cancelled))
                step("заметки", notes(e, c, gold.notes, quick, "заметки", log, cancelled))
                step("голос", notes(e, c, gold.voice, quick, "голос", log, cancelled))
                step("чеки", receipts(e, c, log, cancelled))
            }
            return Report(c.label, if (gpu) "GPU" else "CPU", loadSec, if (sec > 0) tokens / sec else 0.0, peakPss, tempBefore, batteryTemp(), (System.nanoTime() - started) / 1e9, scores)
        } finally {
            sampler.interrupt()
        }
    }

    private fun batteryTemp(): Double {
        val i: Intent? = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return (i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
    }

    private fun ask(engine: Engine, c: Candidate, prompt: String): String {
        val cfg = ConversationConfig(
            systemInstruction = Contents.of("You are a precise parser for a family budget app. Answer with one JSON object only, no comments."),
            samplerConfig = SamplerConfig(1, 1.0, 0.0, 0),
            thinkingConfig = ThinkingConfig(!c.thinkingOff, 0),
        )
        engine.createConversation(cfg).use { conv ->
            val t = System.nanoTime()
            val message = conv.sendMessage(prompt)
            val dt = (System.nanoTime() - t) / 1e9
            val text = message.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
            val decoded = runCatching { conv.getBenchmarkInfo().lastDecodeTokenCount }.getOrDefault(0)
            sec += dt
            tokens += if (decoded > 0) decoded else (text.length / 3).coerceAtLeast(1)
            return text
        }
    }

    private fun <T> pick(all: List<T>, quick: Boolean, every: Int) = if (quick) all.filterIndexed { i, _ -> i % every == 0 } else all

    private fun category(e: Engine, c: Candidate, quick: Boolean, log: (String) -> Unit, cancelled: () -> Boolean): Score? {
        val cats = gold.categories.joinToString(", ")
        var ok = 0
        val cases = pick(gold.payees, quick, 4)
        for ((i, p) in cases.withIndex()) {
            if (cancelled()) return null
            val answer = ask(e, c, "/no_think\nMy categories: $cats.\nThe payee of a purchase is \"${p.payee}\". Pick the one category it belongs to, or \"unknown\". " +
                "Answer as JSON: {\"category\": \"<one of the categories or unknown>\"}")
            val got = jsonOf(answer)?.optString("category") ?: answer
            if (got.trim().equals(p.category, ignoreCase = true) || got.contains(p.category, ignoreCase = true)) ok++
            if (i % 5 == 4) log("  категория ${i + 1}/${cases.size}")
        }
        return Score("category", ok, cases.size)
    }

    private fun notes(e: Engine, c: Candidate, all: List<Gold.Note>, quick: Boolean, name: String, log: (String) -> Unit, cancelled: () -> Boolean): Score? {
        // Quick mode keeps the owner's own style of notes («24000 десерты», «Субе по 30к») and every 3rd of the rest.
        val cases = if (quick) all.filterIndexed { i, n -> n.owner || i % 3 == 0 } else all
        var ok = 0
        var amountOnly = 0
        for ((i, n) in cases.withIndex()) {
            if (cancelled()) return null
            val answer = ask(e, c, "/no_think\nToday is 2026-11-21. Parse one spending note. amount = the amount as digits with a dot for decimals, no spaces, in the units written " +
                "(3 500 -> 3500, 6,99 usd -> 6.99, \"к\"/k = thousand: 30к -> 30000, \"три пятьсот\" -> 3500, \"two thousand five hundred\" -> 2500); " +
                "title = short thing bought, same language; day = 0 today, -1 yesterday, -2 day before yesterday.\n" +
                "Example: \"кофе 1 200 карта\" -> {\"amount\": \"1200\", \"title\": \"кофе\", \"day\": 0}\nNote: \"${n.text}\"")
            val json = jsonOf(answer)
            val amount = minorOf(json?.optString("amount").orEmpty()) == n.amountMinor
            val title = n.title.split(" ").first().lowercase() in json?.optString("title").orEmpty().lowercase()
            val day = (json?.optInt("day", 0) ?: 0) == n.day
            if (amount) amountOnly++
            if (amount && title && day) ok++
            if (i % 5 == 4) log("  $name ${i + 1}/${cases.size}")
        }
        return Score(name, ok, cases.size, "(сумма верна: $amountOnly)")
    }

    private fun receipts(e: Engine, c: Candidate, log: (String) -> Unit, cancelled: () -> Boolean): Score? {
        var ok = 0
        for (r in gold.receipts) {
            if (cancelled()) return null
            val answer = ask(e, c, "/no_think\nRead this receipt. total = the final amount to pay, copied as written (digits, with its separators); currency = ISO code " +
                "(a bare \"$\" in Argentina is ARS); date = purchase date as YYYY-MM-DD; merchant = shop name. " +
                "Answer as JSON: {\"total\": \"\", \"currency\": \"\", \"date\": \"\", \"merchant\": \"\"}\n---\n${r.text}")
            val json = jsonOf(answer) ?: continue
            if (minorOf(json.optString("total")) == r.totalMinor && json.optString("currency") == r.currency && json.optString("date") == r.date &&
                r.merchant.split(" ").first().lowercase() in json.optString("merchant").lowercase()
            ) ok++
        }
        return Score("receipt", ok, gold.receipts.size)
    }
}
