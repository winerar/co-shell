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
 * Until the picker ships (mockup first, owner 2026-11-14) the app stays Russian, as decided in #51:
 * [FOLLOW_PHONE] is off, otherwise a phone in Spanish would switch with no way back.
 */
object AppLanguage {
    /** Languages the app has strings for; the first one is in `res/values`. */
    val SUPPORTED = listOf("ru", "en", "es")

    /** Neither the choice nor any phone language is supported → English. */
    const val FALLBACK = "en"

    /** Switch on together with the language picker and the first-launch question. */
    internal const val FOLLOW_PHONE = false

    private const val PREFS = "app_language"
    private const val KEY = "tag"

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

    /** Tag of the language in effect (after [wrap]): names the starter categories in that language. */
    fun current(): String = Locale.getDefault().language.takeIf { it in SUPPORTED } ?: FALLBACK

    /**
     * [base] with the app language applied — call from `attachBaseContext` of the Application and of
     * every Activity. Also sets the JVM default, so `java.time` formatters name months in this language.
     */
    fun wrap(base: Context): Context {
        val tag = if (FOLLOW_PHONE) pick(chosen(base), phoneLanguages()) else SUPPORTED.first()
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
