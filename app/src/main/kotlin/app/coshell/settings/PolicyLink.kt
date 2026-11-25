package app.coshell.settings

/**
 * The privacy policy page on the app's site (#652, board `Priv652A`): `<site>/privacy/<language>/`, written by `scripts/privacy_pages.py`.
 * The site has ru/en/es pages; any other language opens the English one. `null` — this build does not know the site (no row then).
 */
object PolicyLink {
    val LANGUAGES = listOf("ru", "en", "es")
    private const val FALLBACK = "en"

    fun of(site: String, language: String): String? {
        val base = site.trim().trimEnd('/')
        if (!base.startsWith("https://")) return null
        val lang = language.takeIf { it in LANGUAGES } ?: FALLBACK
        return "$base/privacy/$lang/"
    }
}
