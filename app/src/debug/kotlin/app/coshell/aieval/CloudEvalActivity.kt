package app.coshell.aieval

import androidx.activity.ComponentActivity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.FileProvider
import app.coshell.ai.cloud.CloudLanguageModel
import app.coshell.core.data.auth.AuthGateway
import app.coshell.family.data.UrlConnectionHttpClient
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Debug builds only (#742): runs the AI gold set (`tools/ai-eval`, `docs/samples/ai/eval.json`) through the **cloud** model with the
 * signed-in family account — the phone bench (`tools/aibench`) cannot reach the Worker. A developer tool with its own launcher icon,
 * plain views and Russian text right here; not part of the app's screens. ~70 requests count against the person's daily cloud limit.
 */
@AndroidEntryPoint
class CloudEvalActivity : ComponentActivity() {
    @Inject lateinit var auth: AuthGateway
    @Inject @field:Named("aiProxyUrl") lateinit var proxyUrl: String

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var log: TextView
    private val report = StringBuilder()
    private var answers: File? = null
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 64, 32, 32) }
        col.addView(TextView(this).apply { text = "Проверка облачного ИИ"; textSize = 20f; typeface = Typeface.DEFAULT_BOLD })
        col.addView(TextView(this).apply { text = "Набор заметок, получателей и чеков через код приложения и облако (~70 запросов из дневного лимита)."; textSize = 13f })
        val row = LinearLayout(this)
        row.addView(Button(this).apply { text = "Прогнать"; setOnClickListener { run() } })
        row.addView(Button(this).apply { text = "Поделиться"; setOnClickListener { share() } })
        col.addView(row)
        log = TextView(this).apply { textSize = 12f; typeface = Typeface.MONOSPACE; setTextIsSelectable(true) }
        col.addView(log)
        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun say(line: String) {
        report.appendLine(line)
        log.append(line + "\n")
    }

    private fun run() {
        if (running) return
        val cloud = CloudLanguageModel(UrlConnectionHttpClient(timeoutMs = 40_000), auth, proxyUrl, { auth.session.value != null })
        if (proxyUrl.isBlank()) return say("В этой сборке нет адреса облака (AI_PROXY_URL).")
        if (!cloud.available) return say("Войдите в семью: облаку нужен вход («Ещё → Семья»).")
        running = true
        report.clear(); log.text = ""
        scope.launch {
            try {
                val gold = withContext(Dispatchers.IO) {
                    fun asset(path: String) = assets.open(path).bufferedReader().use { it.readText() }
                    EvalGold(asset("ai/eval.json")) { asset(it.removePrefix("../")) }
                }
                val rules = withContext(Dispatchers.Default) { AiEval(gold, null).all() }
                say("Без ИИ (только правила): " + rules.joinToString(" · ") { it.short })
                val recording = RecordingModel(cloud)
                val eval = AiEval(gold, recording)
                val scores = mutableListOf<AiEval.Score>()
                val started = System.nanoTime()
                for (step in listOf<suspend () -> AiEval.Score>({ eval.notes(false) }, { eval.notes(true) }, { eval.payees(false) }, { eval.payees(true) }, { eval.receipts() })) {
                    val s = withContext(Dispatchers.IO) { step() }
                    scores += s
                    say("${s.line} · запросов: ${s.asked}")
                    s.misses.forEach { say("  ✗ $it") }
                    if (cloud.limited) { say("Дневной лимит облака закончился — продолжите завтра."); break }
                }
                say("Итог облака: " + scores.joinToString(" · ") { it.short })
                say("всего %.0f с".format((System.nanoTime() - started) / 1e9))
                answers = withContext(Dispatchers.IO) {
                    File(cacheDir, "aieval").apply { mkdirs() }.resolve("cloud-haiku.json").apply { writeText(recording.toJson("Claude Haiku [cloud]", scores.map { it.short })) }
                }
                say("Нажмите «Поделиться» — отчёт и файл ответов.")
            } catch (e: Exception) {
                say("Ошибка: ${e::class.simpleName}: ${e.message}")
            } finally {
                running = false
            }
        }
    }

    private fun share() {
        val file = answers
        val send = if (file == null) {
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report.toString())
        } else {
            val uri = FileProvider.getUriForFile(this, "$packageName.updates", file)
            Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_TEXT, report.toString()).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(send, "Проверка облака"))
    }
}
