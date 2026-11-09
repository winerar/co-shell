package app.coshell.core.designsystem.category

/**
 * Brand colours of banks and payment apps the family uses, offered for accounts (owner 2026-11-09).
 * Stored in `Account.color` as is (they are outside [CategoryPalette], so they are drawn unchanged in
 * both themes). Shades are approximate — checked by eye against the logos; Ameria — green, to verify
 * on ameriabank.am (the host is not reachable from the agent's sandbox).
 */
object BankColors {
    /** One preset: [name] shown under the badge, [initials] on it, [aliases] — lowercase name parts to match. */
    data class Preset(val name: String, val initials: String, val color: Long, val aliases: List<String> = emptyList())

    data class Group(val title: String, val presets: List<Preset>)

    val groups: List<Group> = listOf(
        Group(
            "Аргентина",
            listOf(
                Preset("Galicia", "GA", 0xFFF26E22),
                Preset("Santander", "SA", 0xFFEC0000),
                Preset("BBVA", "BB", 0xFF1973B8, listOf("frances", "francés")),
                Preset("Nación", "BN", 0xFF005AA7, listOf("nacion", "bna")),
                Preset("Mercado Pago", "MP", 0xFF00B1EA, listOf("mercadopago", "mp")),
                Preset("Ualá", "UA", 0xFF3E4FE0, listOf("uala")),
                Preset("Naranja X", "NX", 0xFFFE5000, listOf("naranja")),
                Preset("Brubank", "BR", 0xFF6A2EE8),
                Preset("Lemon", "LE", 0xFF00E36B),
            ),
        ),
        Group(
            "Армения",
            listOf(
                Preset("Ameria", "AM", 0xFF00A859, listOf("америа", "ameriabank")),
                Preset("Inecobank", "IN", 0xFF0096D6, listOf("ineco", "инеко")),
                Preset("Ardshinbank", "AR", 0xFF004B93, listOf("ardshin", "ардшин")),
                Preset("ACBA", "AC", 0xFF00843D, listOf("акба")),
                Preset("IDBank", "ID", 0xFFE4002B, listOf("idram")),
                Preset("Evocabank", "EV", 0xFF6D2C91, listOf("evoca", "эвока")),
            ),
        ),
        Group(
            "Россия",
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
            "Крипто и кошельки",
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
