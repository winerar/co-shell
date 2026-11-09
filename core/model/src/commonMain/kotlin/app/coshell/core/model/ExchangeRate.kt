package app.coshell.core.model

import kotlinx.datetime.LocalDate

/**
 * How many units of [to] one unit of [from] costs on [date]: `1 from = rate to`.
 *
 * [RateOrigin.MANUAL] rates are typed by a family member (synced, win over downloaded ones);
 * [RateOrigin.DOWNLOADED] rates are a local cache from the rates provider (not synced).
 */
data class ExchangeRate(
    val date: LocalDate,
    val from: CurrencyCode,
    val to: CurrencyCode,
    val rate: Rate,
    val origin: RateOrigin,
) {
    init {
        require(from != to) { "A rate needs two different currencies, got $from" }
    }
}

enum class RateOrigin { MANUAL, DOWNLOADED }
