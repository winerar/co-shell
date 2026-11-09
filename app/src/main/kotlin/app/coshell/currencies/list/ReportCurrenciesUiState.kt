package app.coshell.currencies.list

/** "More → Report currencies": the list (main first) and the "add a currency" dialog. */
data class ReportCurrenciesUiState(
    val isLoading: Boolean = true,
    /** Codes in order; the first one is the main currency. */
    val currencies: List<String> = emptyList(),
    /** Open "add" dialog, `null` when closed. */
    val adding: AddCurrencyForm? = null,
) {
    val canRemove: Boolean get() = currencies.size > 1
}

/** The "add a currency" dialog: a typed code plus chips of used currencies not in the list yet. */
data class AddCurrencyForm(
    val code: String = "",
    val suggestions: List<String> = emptyList(),
    val error: Boolean = false,
)
