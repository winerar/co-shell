package app.coshell.language

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * Language of the app, independent of the phone's (owner's request, winerar/co-shell#51): the whole
 * interface — our strings, dates, Material's built-in texts (calendar, buttons) — is Russian even on a
 * phone set to English or Spanish. English (`values-en`) and Spanish (`values-es`) strings exist and are
 * used only when chosen explicitly: a picker in settings would pass the chosen tag to [resolve]. A new
 * language = its `values-xx/strings*.xml`, its tag in [SUPPORTED] and in `localeFilters` (app/build.gradle.kts).
 */
object AppLanguage {
    /** Languages the app has strings for; the first one is the default. */
    val SUPPORTED = listOf("ru", "en", "es")

    /** The locale for a chosen language tag; unknown or none → the default (Russian). */
    fun resolve(tag: String?): Locale =
        Locale.forLanguageTag(SUPPORTED.firstOrNull { it == tag } ?: SUPPORTED.first())

    /** Tag of the language in effect (after [wrap]): names the starter categories in that language. */
    fun current(): String = Locale.getDefault().language.takeIf { it in SUPPORTED } ?: SUPPORTED.first()

    /**
     * [base] with the app language applied — call from `attachBaseContext` of the Application and of
     * every Activity. Also sets the JVM default, so `java.time` formatters name months in this language.
     */
    fun wrap(base: Context, tag: String? = null): Context {
        val locale = resolve(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }
}
