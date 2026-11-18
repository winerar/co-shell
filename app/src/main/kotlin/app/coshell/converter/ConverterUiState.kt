package app.coshell.converter

import app.coshell.core.domain.settings.ConverterMode

/** One line of the «Все валюты» list: [text] is the number to show (the typed one on the source row), null — no rate. */
data class ConverterRow(val code: String, val text: String?, val isSource: Boolean)

/**
 * The converter sheet (#319, boards `Conv2*`). [active] is the side the keypad types on; [fromText]/[toText] are ready to show —
 * the typed one is grouped («32 000 + 1 500»), the other one is converted, null — no rate for the pair. [isEmpty] — nothing typed
 * yet: the numbers are a grey «0». [options] are the currencies to pick from (main currency first); [rate] — «1 USD = 1 015 ARS»
 * without the codes, null while the pair has no rate.
 */
data class ConverterUiState(
    val isLoading: Boolean = true,
    val mode: ConverterMode = ConverterMode.PAIR,
    val active: ConverterSide = ConverterSide.FROM,
    val from: String = "",
    val to: String = "",
    val fromText: String? = null,
    val toText: String? = null,
    val isEmpty: Boolean = true,
    val rate: String? = null,
    val rows: List<ConverterRow> = emptyList(),
    val options: List<String> = emptyList(),
    /** The currency the keypad types in; the decimal key is hidden for currencies without a fraction. */
    val source: String = "",
)
