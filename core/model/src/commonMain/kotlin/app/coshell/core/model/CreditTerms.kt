package app.coshell.core.model

/**
 * Conditions of a credit card (#686): the [limitMinor] in minor units of the account currency, the [payDay] of the month the
 * payment is due (a day past the end of a short month means its last day) and the [minPayment] rule. Interest and the grace
 * period are not here yet (the second step).
 */
data class CreditTerms(val limitMinor: Long, val payDay: Int, val minPayment: MinPayment) {
    init {
        require(limitMinor > 0) { "Credit limit must be positive" }
        require(payDay in 1..31) { "Pay day must be a day of the month" }
    }
}

/** The minimum payment as the contract states it: a share of the debt or a fixed sum. */
sealed interface MinPayment {
    /** [basisPoints] of the debt: 500 is 5 %. */
    data class Percent(val basisPoints: Int) : MinPayment {
        init {
            require(basisPoints in 1..10_000) { "Percent must be between 0.01 and 100" }
        }
    }

    data class Fixed(val minor: Long) : MinPayment {
        init {
            require(minor > 0) { "Minimum payment must be positive" }
        }
    }
}
