package app.coshell.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.LocalCompactLists
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RowAccount
import app.coshell.transactions.list.TransactionsUiState.Row
import app.coshell.transactions.list.components.TransactionRow

/**
 * «Внешний вид → Строки в списках» (#775, poll «А»): three operation rows drawn by the real [TransactionRow] at the chosen
 * density, so the switch shows what changes in «Операциях» and «Счетах». Rows are an example only: a tap does nothing.
 */
@Composable
internal fun DensitySample(compact: Boolean, modifier: Modifier = Modifier) {
    val rows = SAMPLE.map { s ->
        Row(
            id = s.id,
            kind = s.kind,
            title = stringResource(s.title),
            subtitle = stringResource(s.account),
            note = "",
            amount = s.amount,
            icon = s.icon,
            color = s.color,
            account = RowAccount(stringResource(s.account), s.accountColor),
        )
    }
    CompositionLocalProvider(LocalCompactLists provides compact) {
        CoShellRowGroup(modifier.padding(horizontal = Spacing.l)) {
            rows.forEach { r -> row { TransactionRow(r, onClick = {}, background = Color.Transparent) } }
        }
    }
}

private class SampleRow(
    val id: String,
    val kind: Kind,
    val title: Int,
    val account: Int,
    val amount: String,
    val icon: String,
    val color: Long,
    val accountColor: Long,
)

private val ARS = CurrencyCode("ARS")
private val USD = CurrencyCode("USD")

private fun signed(sign: String, money: Money) = sign + MoneyFormat.RU.format(money)

private val SAMPLE = listOf(
    SampleRow(
        "food", Kind.EXPENSE, R.string.default_category_expense_food, R.string.account_type_card,
        signed("-", Money.ofMajor(32_000, currency = ARS)), "food", 0xFF7FD6A0, 0xFFF26E22,
    ),
    SampleRow(
        "cafe", Kind.EXPENSE, R.string.default_category_expense_restaurant, R.string.account_type_cash,
        signed("-", Money.ofMajor(4_500, currency = ARS)), "cafe", 0xFFFFB86B, 0xFFC9D3C2,
    ),
    SampleRow(
        "salary", Kind.INCOME, R.string.default_category_income_salary, R.string.account_type_card,
        signed("+", Money.ofMajor(1_200, currency = USD)), "salary", 0xFF6BB8FF, 0xFFF26E22,
    ),
)
