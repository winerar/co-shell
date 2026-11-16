package app.coshell.accounts.edit

import app.coshell.core.model.AccountType

/**
 * Stress sample of the account editor (#378): a name of 50+ characters, a billion-sized initial balance, ten
 * suggested currencies, a long balance error and the sharing switch (a family of several).
 */
internal val accountEditStressSample =
    AccountEditUiState(
        isNew = true,
        name = "Тинькофф Накопительный Анастасии для отпуска в Патагонии",
        currency = "USDT",
        type = AccountType.CRYPTO,
        initialBalance = "999999999,99",
        suggestedCurrencies = listOf("USDT", "ARS", "AMD", "RUB", "JPY", "KWD", "BTC", "EUR", "BRL", "CLP"),
        showSharing = true,
        balanceError = AccountEditUiState.BalanceError.TOO_MANY_FRACTION_DIGITS,
    )
