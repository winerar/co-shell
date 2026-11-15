package app.coshell.reports.capital

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.NET_WORTH_MONTHS
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

/**
 * Board `Rp189NwDrill`: 24 accounts in four groups, October picked on a year line — previews and screenshot tests.
 * Built by [buildCapitalMonth] itself, so the sample shows what the screen really makes of such data.
 */
object CapitalMonthSamples {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val usdt = CurrencyCode("USDT")

    private fun acc(id: String, name: String, currency: CurrencyCode, type: AccountType, units: Long, color: Long, owner: String? = null) =
        Account(id, name, currency, type = type, initialBalanceMinor = units * 100, color = color, isShared = owner == null, ownerMemberId = owner)

    private val accounts = listOf(
        acc("galicia", "Galicia", ars, AccountType.CARD, 1_250_000, 0xFFF26F21),
        acc("mp", "Mercado Pago", ars, AccountType.CARD, 96_400, 0xFF00B1EA),
        acc("santander", "Santander", ars, AccountType.CARD, 310_000, 0xFFEC0000),
        acc("ameria", "Ameria", amd, AccountType.CARD, 410_000, 0xFFB5223A, owner = "anya"),
        acc("ineco", "Inecobank", amd, AccountType.CARD, 120_000, 0xFF0096D6),
        acc("bbva", "BBVA", ars, AccountType.CARD, 180_000, 0xFF004481),
        acc("brubank", "Brubank", ars, AccountType.CARD, 45_000, 0xFF6C2BD9),
        acc("tinkoff", "Т-Банк", CurrencyCode("RUB"), AccountType.CARD, 38_000, 0xFFFFDD2D),
        acc("sber", "Сбер", CurrencyCode("RUB"), AccountType.CARD, 22_500, 0xFF21A038),
        acc("uala", "Ualá", ars, AccountType.CARD, 64_000, 0xFF3E6BFF),
        acc("idram", "Idram", amd, AccountType.CARD, 85_000, 0xFFE30613),
        acc("binance", "Binance", usdt, AccountType.CRYPTO, 1_480, 0xFFF3BA2F),
        acc("bybit", "Bybit", usdt, AccountType.CRYPTO, 620, 0xFF17181E),
        acc("lemon", "Lemon", usdt, AccountType.CRYPTO, 330, 0xFF00F08C),
        acc("usd_cash", "Доллары дома", usd, AccountType.CASH, 1_200, 0xFF4CAF50),
        acc("ars_cash", "Песо в кошельке", ars, AccountType.CASH, 85_000, 0xFF8BC34A),
        acc("amd_cash", "Драмы", amd, AccountType.CASH, 60_000, 0xFFCDDC39),
        acc("rub_cash", "Рубли", CurrencyCode("RUB"), AccountType.CASH, 15_000, 0xFF9E9E9E),
        acc("trip", "На поездку", usd, AccountType.CASH, 300, 0xFF00BCD4),
        acc("misha_cash", "Карманные", ars, AccountType.CASH, 40_000, 0xFF795548, owner = "misha"),
        acc("deposit", "Вклад Ameria", amd, AccountType.DEPOSIT, 600_000, 0xFF673AB7),
        acc("plazo", "Plazo fijo", ars, AccountType.DEPOSIT, 900_000, 0xFF3F51B5),
        acc("loan", "Долг Лёше", usd, AccountType.DEPOSIT, -300, 0xFFE91E63),
        acc("fci", "FCI Galicia", ars, AccountType.DEPOSIT, 250_000, 0xFF009688),
    )

    private fun rate(from: CurrencyCode, value: String) = ExchangeRate(LocalDate(2025, 11, 1), usd, from, Rate.parse(value), RateOrigin.DOWNLOADED)

    private val book = RateBook.of(listOf(rate(ars, "1025"), rate(amd, "387"), rate(CurrencyCode("RUB"), "81"), rate(usdt, "1")))

    /** Savings into the dollars every month, a bigger one in November: the line rises. */
    private val days = (1..12).map { m ->
        val month = YearMonth(2025, 11).plus(m, DateTimeUnit.MONTH)
        BalanceDay(LocalDate(month.year, month.month, 10), "usd_cash", if (m == 12) 500_00 else (m % 4) * 60_00L)
    }

    private val input = CapitalInput(
        accounts = accounts,
        days = days,
        book = book,
        currency = usd,
        periods = reportMonths(YearMonth(2026, 11), NET_WORTH_MONTHS),
        today = LocalDate(2026, 11, 15),
        others = mapOf("anya" to "Аня", "misha" to "Миша"),
    )

    private val format = MoneyFormat(trimZeroFraction = true)

    val state = buildCapitalMonth(input, CapitalView(YearMonth(2026, 10)), format, format)
    val byChange = buildCapitalMonth(input, CapitalView(YearMonth(2026, 11), order = CapitalOrder.CHANGE), format, format)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CapitalMonthPreview() = CoShellTheme(darkTheme = true) { CapitalMonthScreen(CapitalMonthSamples.state, CapitalMonthActions()) }
