package app.coshell.transactions.list

import app.coshell.core.domain.search.FilterTotal
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Total

/**
 * Texts of the total under the filters (4А). One currency: its net, «≈ X USD» under it when the report
 * currency differs. Several: «≈ X USD» on top (each operation at the rate of its day), the nets of each
 * currency under it; currencies left out of «≈» for lack of any rate go to [Total.noRate] (the screen says
 * «без курса: AMD»), so a partial sum never passes for the whole.
 */
internal fun FilterTotal.toUi(moneyFormat: MoneyFormat): Total {
    fun signed(m: Money) = if (m.isPositive) "+" + moneyFormat.format(m) else moneyFormat.format(m)
    val missing = converted?.missing.orEmpty().takeIf { it.isNotEmpty() }?.joinToString { it.value }
    val approx = converted?.takeIf { !it.sum.isZero }?.let { "≈ " + signed(it.sum) }
    return when (nets.size) {
        0 -> Total(count)
        1 -> {
            val net = nets.single()
            val other = approx?.takeIf { converted?.sum?.currency != net.currency && missing == null }
            // The net itself is exact; without a rate there is just no «≈» under it.
            Total(count, signed(net), other, net.tone())
        }
        else -> {
            val main = converted?.sum
            val top = approx ?: signed(nets.first())
            Total(
                count = count,
                amount = top,
                // One currency per line: a long line would break inside an amount.
                detail = nets.joinToString("\n", transform = ::signed),
                tone = (main ?: nets.first()).tone(),
                noRate = missing,
            )
        }
    }
}

private fun Money.tone(): Kind? = when {
    isNegative -> Kind.EXPENSE
    isPositive -> Kind.INCOME
    else -> null
}
