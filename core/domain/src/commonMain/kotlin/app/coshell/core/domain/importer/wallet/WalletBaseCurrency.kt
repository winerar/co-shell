package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.CurrencyCode

/**
 * Wallet's base currency is not named in the file: it is the currency whose records always have
 * `ref_currency_amount == amount` (AMD in the owner's export). `null` if none or ambiguous.
 */
object WalletBaseCurrency {
    fun detect(records: List<WalletRecord>): CurrencyCode? =
        records.groupBy { it.amount.currency }
            .filterValues { rows -> rows.all { it.refAmount.toMoney(it.amount.currency) == it.amount } }
            .maxByOrNull { it.value.size }
            ?.key
}
