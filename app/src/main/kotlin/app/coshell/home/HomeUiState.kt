package app.coshell.home

data class HomeUiState(
    val isLoading: Boolean = false,
    /** One formatted total per currency, e.g. ["125 300,00 ₽", "1 250,00 €"]. */
    val totals: List<String> = emptyList(),
    val accounts: List<AccountRow> = emptyList(),
) {
    data class AccountRow(val id: String, val name: String, val balance: String, val isNegative: Boolean)
}
