package app.coshell.language

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * Language of the app, independent of the phone's (owner's request, winerar/co-shell#51): the whole
 * interface — our strings, dates, Material's built-in texts (calendar, buttons) — is Russian even on a
 * phone set to English or Spanish. One place to change: a new language = its `values-xx/strings.xml`
 * plus its tag in [SUPPORTED]; a picker in «Настройки» would pass the chosen tag to [resolve].
 */
object AppLanguage {
    /** Languages the app has strings for; the first one is the default. */
    val SUPPORTED = listOf("ru")

    /** The locale for a chosen language tag; unknown or none → the default (Russian). */
    fun resolve(tag: String?): Locale =
        Locale.forLanguageTag(SUPPORTED.firstOrNull { it == tag } ?: SUPPORTED.first())

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
