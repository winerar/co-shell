package app.coshell.core.model

/**
 * Conditions of a credit card (#686): the [limitMinor] in minor units of the account currency, the [payDay] of the month the
 * payment is due (a day past the end of a short month means its last day) and the [minPayment] rule. The grace period is a
 * reminder only (#634, step 2): the [statementDay] and the [graceDays] after it, both or neither; no interest is counted.
 */
data class CreditTerms(
    val limitMinor: Long,
    val payDay: Int,
    val minPayment: MinPayment,
    val statementDay: Int? = null,
    val graceDays: Int? = null,
) {
    init {
        require((statementDay == null) == (graceDays == null)) { "Statement day and grace days go together" }
        statementDay?.let { require(it in 1..31) { "Statement day must be a day of the month" } }
        graceDays?.let { require(it in 1..365) { "Grace period must be 1 to 365 days" } }
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
