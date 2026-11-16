package app.coshell.reports

/**
 * Stress sample of «Отчёты» (#378): [ReportsSamples] with 40+ character category names, a word without spaces,
 * billion-sized amounts, a long name of who added it and ten currencies to pick from.
 */
internal val reportsStressSample: ReportsUiState = run {
    val base = ReportsSamples.state
    val spending = ReportsSamples.spending
    val names = listOf(
        "Коммунальные платежи и обслуживание квартиры",
        "Продуктыиздомашнегомагазинабезпробеловвообще",
        "Образование детей: кружки, секции и репетиторы",
    )
    val amount = "999 999 999"
    fun rename(i: Int, name: String?) = if (i < names.size) names[i] else name
    base.copy(
        currency = "USDT",
        currencies = listOf("USDT", "ARS", "AMD", "RUB", "JPY", "KWD", "BTC", "EUR", "BRL", "CLP"),
        spending = spending.copy(
            total = "999 999 999,99",
            bars = spending.bars.mapIndexed { i, b -> b.copy(name = rename(i, b.name), value = amount) },
            categories = spending.categories.mapIndexed { i, c -> c.copy(name = rename(i, c.name), value = amount) },
        ),
        members = ReportsSamples.members.mapIndexed { i, m ->
            if (i == 0) m.copy(name = "Анастасия Константиновна-Вайншейкер", value = amount) else m.copy(value = amount)
        },
    )
}
