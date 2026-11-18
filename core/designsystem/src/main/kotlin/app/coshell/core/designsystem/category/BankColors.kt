package app.coshell.core.designsystem.category

/**
 * Brand colours of banks and payment apps the family uses, offered for accounts (owner 2026-11-09).
 * Stored in `Account.color` as is (they are outside [CategoryPalette], so they are drawn unchanged in
 * both themes). 2026-11-18: checked against each service's own icon (Google/DuckDuckGo favicon, dominant
 * saturated colour); kept as is where within noise, or where no icon showed the brand (Inecobank, VTB,
 * Revolut, Tether — official mark colour).
 */
object BankColors {
    /** One preset: [name] shown under the badge, [initials] on it, [aliases] — lowercase name parts to match. */
    data class Preset(val name: String, val initials: String, val color: Long, val aliases: List<String> = emptyList())

    /** [title] is an internal label for reading this file — not shown in the UI. */
    data class Group(val title: String, val presets: List<Preset>)

    val groups: List<Group> = listOf(
        Group(
            "Argentina",
            listOf(
                Preset("Galicia", "GA", 0xFFF26E22),
                Preset("Santander", "SA", 0xFFEC0000),
                Preset("BBVA", "BB", 0xFF00278E, listOf("frances", "francés")),
                Preset("Nación", "BN", 0xFF00738D, listOf("nacion", "bna")),
                Preset("Mercado Pago", "MP", 0xFF00B1EA, listOf("mercadopago", "mp")),
                Preset("Ualá", "UA", 0xFF062095, listOf("uala")),
                Preset("Naranja X", "NX", 0xFFFE5000, listOf("naranja")),
                Preset("Brubank", "BR", 0xFF6A2EE8),
                Preset("Lemon", "LE", 0xFF00E36B),
            ),
        ),
        Group(
            "Armenia",
            listOf(
                Preset("Ameria", "AM", 0xFF68B92D, listOf("америа", "ameriabank")),
                Preset("Inecobank", "IN", 0xFF0096D6, listOf("ineco", "инеко")),
                Preset("Ardshinbank", "AR", 0xFF0A61C7, listOf("ardshin", "ардшин")),
                Preset("ACBA", "AC", 0xFF00A789, listOf("акба")),
                Preset("IDBank", "ID", 0xFFF5821F, listOf("idram")),
                Preset("Evocabank", "EV", 0xFF6200DA, listOf("evoca", "эвока")),
            ),
        ),
        Group(
            "Russia",
            listOf(
                Preset("Сбер", "СБ", 0xFF21A038, listOf("сбербанк", "sber")),
                Preset("Т-Банк", "Т", 0xFFFFDD2D, listOf("тинькофф", "тбанк", "tinkoff", "t-bank")),
                Preset("Альфа", "А", 0xFFEF3124, listOf("альфа-банк", "alfa")),
                Preset("ВТБ", "ВТБ", 0xFF0A2896, listOf("vtb")),
                Preset("Ozon Банк", "OZ", 0xFF005BFF, listOf("ozon", "озон")),
                Preset("Райффайзен", "Р", 0xFFFEE600, listOf("raiffeisen")),
            ),
        ),
        Group(
            "Crypto and wallets",
            listOf(
                Preset("Binance", "BN", 0xFFF0B90B),
                Preset("Bybit", "BY", 0xFFF7A600),
                Preset("Tether", "₮", 0xFF26A17B, listOf("usdt")),
                Preset("Wise", "W", 0xFF9FE870),
                Preset("PayPal", "PP", 0xFF003087),
                Preset("Payoneer", "PY", 0xFFFF4800),
                Preset("Revolut", "R", 0xFF0075EB),
            ),
        ),
    )

    val presets: List<Preset> = groups.flatMap { it.presets }

    /**
     * The preset whose name or alias is a word of [accountName] ("Galicia ARS", "карта Сбер"), for the
     * «Похоже на банк …» hint; `null` when nothing matches.
     */
    fun suggest(accountName: String): Preset? {
        val words = accountName.lowercase().split(' ', '·', '-', '_', '/', ',').filter { it.isNotBlank() }.toSet()
        val text = accountName.lowercase()
        return presets.firstOrNull { preset ->
            val keys = preset.aliases + preset.name.lowercase()
            keys.any { key -> if (' ' in key || '-' in key) key in text else key in words }
        }
    }

    /** Preset with exactly this colour, to name it in the editor. */
    fun byColor(color: Long?): Preset? = presets.firstOrNull { it.color == color }
}
