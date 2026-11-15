package app.coshell.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

/**
 * Language of the app (winerar/co-shell#270): the whole interface — our strings, dates, Material's built-in
 * texts — speaks one of [SUPPORTED]. Order: the language chosen in the app → the phone's language (the first
 * supported one in its list) → [FALLBACK]. A new language = its `values-xx/strings*.xml`, its tag in [SUPPORTED]
 * and in `localeFilters` (app/build.gradle.kts); `TranslationsTest` checks all three agree.
 *
 * The choice lives in «Ещё → Внешний вид» and is asked once in a sheet over «Обзор» (#352, board Fb352A): on the
 * first launch with the phone's language preselected, and once after the update for those who used the app
 * in Russian before — until they answer, their app stays Russian (nobody's language changes silently).
 */
object AppLanguage {
    /** Languages the app has strings for; the first one is in `res/values`. */
    val SUPPORTED = listOf("ru", "en", "es")

    /** Neither the choice nor any phone language is supported → English. */
    const val FALLBACK = "en"

    private const val PREFS = "app_language"
    private const val KEY = "tag"
    private const val KEY_PROMPT = "prompt"

    /** Tag to use: [chosen] if supported, else the first supported of [phone] (tags like `es-AR`), else [FALLBACK]. */
    fun pick(chosen: String?, phone: List<String>): String =
        chosen?.takeIf { it in SUPPORTED }
            ?: phone.map { it.substringBefore('-').substringBefore('_').lowercase() }.firstOrNull { it in SUPPORTED }
            ?: FALLBACK

    /** Languages of the phone, most preferred first — not affected by [wrap]. */
    fun phoneLanguages(): List<String> =
        Resources.getSystem().configuration.locales.let { list -> (0 until list.size()).map { list[it].toLanguageTag() } }

    /** The language chosen in the app; `null` = follow the phone. Synchronous: readable in `attachBaseContext`. */
    fun chosen(context: Context): String? = prefs(context).getString(KEY, null)

    /** Saves the choice (`null` = follow the phone). The caller recreates the activity to apply it. */
    fun choose(context: Context, tag: String?) {
        prefs(context).edit().apply { if (tag == null) remove(KEY) else putString(KEY, tag) }.apply()
    }

    /**
     * Starting state for a phone that has never been asked: one who used the app before (an update) keeps
     * Russian and gets [LanguagePrompt.UPDATE]; a new install follows the phone and gets [LanguagePrompt.FIRST].
     * If the phone already speaks Russian, an update has nothing to ask.
     */
    fun initial(updated: Boolean, phone: List<String>): Pair<String?, LanguagePrompt> = when {
        !updated -> null to LanguagePrompt.FIRST
        pick(null, phone) == SUPPORTED.first() -> null to LanguagePrompt.NONE
        else -> SUPPORTED.first() to LanguagePrompt.UPDATE
    }

    /** Which question the sheet over «Обзор» should ask, if any. */
    fun prompt(context: Context): LanguagePrompt =
        prefs(context).getString(KEY_PROMPT, null)?.let { saved -> LanguagePrompt.entries.firstOrNull { it.name == saved } }
            ?: LanguagePrompt.NONE

    /** The answer to the sheet: saves [tag] (`null` = follow the phone); nothing is asked again. */
    fun answer(context: Context, tag: String?) {
        choose(context, tag)
        prefs(context).edit().putString(KEY_PROMPT, LanguagePrompt.NONE.name).apply()
    }

    /** Tag of the language in effect (after [wrap]): names the starter categories in that language. */
    fun current(): String = Locale.getDefault().language.takeIf { it in SUPPORTED } ?: FALLBACK

    /** [context] speaking [tag] — for strings in a language other than the one in effect (starter category names). */
    fun localized(context: Context, tag: String): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(Locale.forLanguageTag(tag)))
        return context.createConfigurationContext(config)
    }

    /**
     * [base] with the app language applied — call from `attachBaseContext` of the Application and of
     * every Activity. Also sets the JVM default, so `java.time` formatters name months in this language.
     */
    fun wrap(base: Context): Context {
        setUpOnce(base)
        val tag = pick(chosen(base), phoneLanguages())
        Locale.setDefault(Locale.forLanguageTag(tag))
        return localized(base, tag)
    }

    /** Writes [initial] the first time the app starts with this code; later starts read what was saved. */
    private fun setUpOnce(context: Context) {
        val prefs = prefs(context)
        if (prefs.contains(KEY_PROMPT)) return
        val (tag, prompt) = initial(wasUpdated(context), phoneLanguages())
        prefs.edit().apply { if (tag != null) putString(KEY, tag) }.putString(KEY_PROMPT, prompt.name).commit()
    }

    /** `true` when the app was installed earlier and then updated — someone already used it (in Russian). */
    private fun wasUpdated(context: Context): Boolean = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.lastUpdateTime > info.firstInstallTime
    }.getOrDefault(false)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** The one-time language question: on the first launch, once after the update, or none. */
enum class LanguagePrompt { FIRST, UPDATE, NONE }
