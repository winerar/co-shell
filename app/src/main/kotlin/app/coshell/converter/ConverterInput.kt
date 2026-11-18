package app.coshell.converter

import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.settings.ConverterMode
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode

/** The side of the pair the keypad types on: «Отдаю» or «Получаю». */
enum class ConverterSide { FROM, TO }

/**
 * What the converter sheet holds (#319, boards `Conv2*`): the keypad [text] («100», «32000 + 1500»; empty — nothing typed yet),
 * the [active] side it belongs to and the pair [from] → [to]. In [ConverterMode.ALL] the source row is [from]. The pair never
 * holds one currency twice (unless the list has only one).
 */
internal data class ConverterInput(
    val from: CurrencyCode,
    val to: CurrencyCode,
    val text: String = "",
    val active: ConverterSide = ConverterSide.FROM,
) {
    /** The currency the typed number is in. */
    val source: CurrencyCode get() = if (active == ConverterSide.FROM) from else to
    val target: CurrencyCode get() = if (active == ConverterSide.FROM) to else from

    fun type(key: KeypadKey): ConverterInput = copy(text = AmountKeys.apply(text, key, Currencies.info(source).fractionDigits))

    /** A tap on the other side: typing starts afresh there (a number of one currency means nothing in the other). */
    fun activate(side: ConverterSide): ConverterInput = if (side == active) this else copy(active = side, text = "")

    /** «⇅»: the currencies change places, the number stays where it is. */
    fun swap(): ConverterInput = copy(from = to, to = from).let { it.copy(text = fitFraction(it.text, it.source)) }

    /** [code] for one [side]; the other side's currency chosen — they swap, so the pair stays two different ones. */
    fun pick(side: ConverterSide, code: CurrencyCode): ConverterInput {
        val other = if (side == ConverterSide.FROM) to else from
        if (code == other) return swap()
        val next = if (side == ConverterSide.FROM) copy(from = code) else copy(to = code)
        return if (side == active) next.copy(text = fitFraction(text, code)) else next
    }

    /** The list shows [code] as the source row: [from] becomes it, the old source takes its place in the pair. */
    fun source(code: CurrencyCode): ConverterInput {
        if (code == from) return this
        val next = if (code == to) copy(from = code, to = from) else copy(from = code)
        return next.copy(text = fitFraction(text, code), active = ConverterSide.FROM)
    }

    /** The list needs the typed number on [from]: with the number on «Получаю» the pair swaps and nothing typed is lost. */
    fun forAll(): ConverterInput = if (active == ConverterSide.TO) copy(from = to, to = from, active = ConverterSide.FROM) else this

    companion object {
        /** A currency without a fraction (JPY) cannot keep «12,5»: the fraction of every term goes («12,5 + 3,25 − 1,5» → «12 + 3 − 1»). */
        fun fitFraction(text: String, currency: CurrencyCode): String =
            if (Currencies.info(currency).fractionDigits == 0) AmountKeys.mapTerms(text) { it.substringBefore(',') } else text
    }
}
