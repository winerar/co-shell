package app.coshell.accounts.list

import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat

/**
 * Stress sample of «Счета» (#378): long names and a word without spaces, a member's long name, billion-sized and
 * negative balances, zero, JPY / KWD / BTC, and a group of 30 accounts — the list must stay readable and fast.
 */
internal val accountsStressSample: AccountsUiState = run {
    fun m(minor: Long, code: String) = MoneyFormat.RU.format(Money(minor, CurrencyCode(code)))
    val long = AccountsUiState.Row(
        "l1", "Тинькофф Накопительный Анастасии для отпуска в Патагонии", AccountType.DEPOSIT, m(999_999_999_99, "RUB"),
        isNegative = false, personalOf = "Анастасия Каренина-Толстая", color = 0xFFFFDD2D, converted = "≈\u00A0${m(12_345_678_90, "USD")}",
    )
    val word = AccountsUiState.Row(
        "l2", "Кредиткавдолларахбезпробеловвообще", AccountType.CARD, m(-999_999_999_99, "USD"),
        isNegative = true, excludedFromTotal = true, personalOf = "",
    )
    val zero = AccountsUiState.Row("l3", "Пусто", AccountType.CASH, m(0, "ARS"), isNegative = false)
    val jpy = AccountsUiState.Row("l4", "Токио", AccountType.CASH, m(1_234_567, "JPY"), false, converted = "≈\u00A0${m(8_123_45, "USD")}")
    val kwd = AccountsUiState.Row("l5", "Кувейт", AccountType.CARD, m(12_345_678, "KWD"), false, converted = "≈\u00A0${m(40_123_45, "USD")}")
    val btc = AccountsUiState.Row("l6", "Холодный кошелёк", AccountType.CRYPTO, m(12_345_678_901, "BTC"), false, converted = "≈\u00A0${m(999_999_999_99, "USD")}")
    val many = List(30) { i ->
        AccountsUiState.Row("m$i", "Счёт номер ${i + 1}", AccountType.OTHER, m((i + 1) * 1_000_00L, "ARS"), false, converted = "≈\u00A0${m((i + 1) * 1_00L, "USD")}")
    }
    val top = listOf(long, word, zero, jpy, kwd, btc)
    AccountsUiState(
        active = top + many,
        total = "≈\u00A0${m(-999_999_999_99, "USD")}",
        groups = listOf(
            AccountsUiState.Group("TYPE:CARD", titleText = "Карты, банки, вклады и всё остальное, что не наличные", rows = top, total = "≈\u00A0${m(999_999_999_99, "USD")}", expanded = true),
            AccountsUiState.Group("TYPE:OTHER", titleText = "Другое", rows = many, total = "≈\u00A0${m(465_00, "USD")}", expanded = true),
        ),
        archived = listOf(AccountsUiState.Row("a1", "Старая карта с очень длинным названием, которую закрыли", AccountType.CARD, m(0, "RUB"), false)),
    )
}

/** No accounts yet — the empty state with its hint. */
internal val accountsEmptySample = AccountsUiState()
