package app.coshell.aibench

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

/**
 * One screen: device line, model list (download / delete / test), options, log and «copy report».
 * Plain views on purpose — a tool, not the app: no Compose, no design system, text in Russian right here.
 */
class MainActivity : Activity() {
    private lateinit var log: TextView
    private lateinit var scroll: ScrollView
    private lateinit var gpu: CheckBox
    private lateinit var quick: CheckBox
    private lateinit var token: EditText
    private val rows = mutableMapOf<String, Pair<TextView, Button>>()
    private val report = StringBuilder()
    @Volatile private var busy = false
    @Volatile private var cancel = false

    private val dir by lazy { File(getExternalFilesDir(null) ?: filesDir, "models").also { it.mkdirs() } }
    private fun fileOf(c: Candidate) = File(dir, c.file)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val ramMb = mem.totalMem / 1_000_000
        val device = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} · ${if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else Build.HARDWARE} · ОЗУ $ramMb МБ"
        report.appendLine(device)

        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 48, 24, 24) }
        col.addView(text("co-shell AI bench", 22f, bold = true))
        col.addView(text(device, 12f))
        col.addView(text("По памяти: ${tierFor(ramMb)}", 14f))
        gpu = CheckBox(this).apply { text = "GPU вместо процессора (если телефон умеет)" }
        quick = CheckBox(this).apply { text = "Быстрый тест (≈ 30 примеров, 5–10 мин)"; isChecked = true }
        col.addView(gpu); col.addView(quick)
        token = EditText(this).apply {
            hint = "Токен Hugging Face (только для Gemma 3n)"; textSize = 12f; setSingleLine()
            setText(getSharedPreferences("bench", MODE_PRIVATE).getString("hf_token", ""))
        }
        col.addView(token)
        for (c in CANDIDATES) col.addView(modelRow(c))
        val bottom = LinearLayout(this).apply { gravity = Gravity.CENTER }
        bottom.addView(Button(this).apply { text = "Остановить"; setOnClickListener { cancel = true } })
        bottom.addView(Button(this).apply { text = "Скопировать отчёт"; setOnClickListener { copy() } })
        bottom.addView(Button(this).apply { text = "Поделиться"; setOnClickListener { share() } })
        col.addView(bottom)
        log = text("", 12f).apply { typeface = Typeface.MONOSPACE; setTextIsSelectable(true) }
        col.addView(log)
        scroll = ScrollView(this).apply { addView(col) }
        setContentView(scroll)
        refresh()
    }

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; setPadding(0, 6, 0, 6); if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun modelRow(c: Candidate): LinearLayout {
        val label = text("", 14f).apply { layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val action = Button(this)
        val test = Button(this).apply { text = "Тест"; setOnClickListener { runTest(c) } }
        rows[c.id] = label to action
        action.setOnClickListener {
            if (busy) return@setOnClickListener
            if (fileOf(c).exists()) { fileOf(c).delete(); refresh() } else download(c)
        }
        return LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; addView(label); addView(action); addView(test) }
    }

    private fun refresh() = runOnUiThread {
        for (c in CANDIDATES) {
            val (label, action) = rows.getValue(c.id)
            val have = fileOf(c).exists()
            label.text = "${c.label} · ${c.sizeMb} МБ" + if (have) " ✓" else ""
            action.text = if (have) "Удалить" else "Скачать"
        }
    }

    private fun say(s: String) {
        report.appendLine(s)
        runOnUiThread { log.append(s + "\n"); scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) } }
    }

    private fun download(c: Candidate) {
        busy = true; cancel = false
        say("Скачиваю ${c.label}…")
        Thread {
            try {
                var last = -1
                val hf = token.text.toString().also { getSharedPreferences("bench", MODE_PRIVATE).edit().putString("hf_token", it).apply() }
                Downloader.download(c.url, fileOf(c), if (c.gated) hf else "", { done, total ->
                    val pct = if (total > 0) (done * 100 / total).toInt() else 0
                    if (pct != last && pct % 5 == 0) { last = pct; say("  $pct % (${done / 1_000_000} из ${total / 1_000_000} МБ)") }
                }, { cancel })
                say(if (fileOf(c).exists()) "Скачано: ${c.label}" else "Остановлено (докачается с того же места)")
            } catch (e: Exception) {
                say("Не удалось скачать: ${e.message}. Нажмите «Скачать» ещё раз — докачается.")
            } finally { busy = false; refresh() }
        }.start()
    }

    private fun runTest(c: Candidate) {
        if (busy) return
        if (!fileOf(c).exists()) return say("Сначала скачайте ${c.label}")
        busy = true; cancel = false
        say("\n=== ${c.label} (${if (gpu.isChecked) "GPU" else "CPU"}, ${if (quick.isChecked) "быстрый" else "полный"}) ===")
        Thread {
            try {
                val r = Bench(this, Gold(this)).run(c, fileOf(c), gpu.isChecked, quick.isChecked, ::say) { cancel }
                say("Итог ${r.model} [${r.backend}]: ${r.scores.joinToString(" · ") { "${it.name} ${it.ok}/${it.of}" }}")
                say("скорость %.1f ток/с · память (PSS) до %d МБ · загрузка %.1f с · всего %.0f с · батарея %.1f→%.1f °C".format(
                    r.tokPerSec, r.peakPssMb, r.loadSec, r.totalSec, r.tempBefore, r.tempAfter))
            } catch (e: Throwable) {
                say("Ошибка: ${e::class.simpleName}: ${e.message}")
            } finally { busy = false }
        }.start()
    }

    private fun copy() {
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("aibench", report.toString()))
        say("Отчёт скопирован — вставьте его в чат.")
    }

    private fun share() {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report.toString()), "Отчёт"))
    }
}
