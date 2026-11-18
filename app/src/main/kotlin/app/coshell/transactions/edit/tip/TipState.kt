package app.coshell.transactions.edit.tip

import app.coshell.core.domain.quickentry.Tips
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.transactions.edit.parsePositiveAmount
import kotlinx.datetime.LocalDate

/**
 * The «Чаевые» sheet over the entry sheet (#322, boards `Fb322Tips`, `Fb322First`): [percent] 10/15 or `null` — «Своя сумма»
 * typed into [own]; the account the tip is paid from and its category (`null` — not chosen yet, [asking] shows the first-time
 * choice). The texts are filled by [refreshed] from the purchase and the rates, so the screen only shows them.
 */
data class TipUi(
    val percent: Int? = Tips.PERCENTS.first(),
    val own: String = "",
    val accountId: String? = null,
    val categoryId: String? = null,
    /** The category choice is open (the first time, or the row was tapped). */
    val asking: Boolean = false,
    /** The account choice is open. */
    val pickingAccount: Boolean = false,
    /** The receipt amount the percent is taken from, e.g. «13 300 ARS». */
    val baseText: String = "",
    /** The tip in the receipt currency, e.g. «1 330 ARS»; empty for an own amount not typed yet. */
    val tipText: String = "",
    /** The tip in the account's currency when it differs, e.g. «1,31 USD»; `null` — same currency. */
    val convertedText: String? = null,
    /** The account's currency differs and no rate is known: the tip cannot be saved. */
    val noRate: Boolean = false,
    /** A tip is entered and can be saved. */
    val canSave: Boolean = false,
    /** Saving the tip failed after the purchase was saved. */
    val failed: Boolean = false,
)

/** The line «Чаевые» (a new category) of the first-time category choice. */
const val NEW_TIP_CATEGORY = "@new"

/** What the sheet needs to know about the purchase it adds a tip to. */
internal class TipContext(
    val main: Transaction,
    val date: LocalDate,
    val accountsById: Map<String, Account>,
    val converter: CurrencyConverter?,
    val format: MoneyFormat,
)

/** The tip typed or chosen, in the currency of the receipt; `null` — nothing valid yet. */
internal fun TipUi.tipMoney(main: Transaction): Money? {
    val base = Tips.base(main)
    return if (percent != null) Tips.percentOf(base, percent) else parsePositiveAmount(own, base.currency).money
}

/** [this] with every text recomputed: the percent of the receipt, the conversion to the account's currency, [TipUi.canSave]. */
internal fun TipUi.refreshed(ctx: TipContext): TipUi {
    val tip = tipMoney(ctx.main)
    val account = accountId?.let { ctx.accountsById[it] }
    val converted = if (tip != null && account != null && tip.currency != account.currency) convert(ctx, tip, account.currency) else null
    val missingRate = tip != null && account != null && tip.currency != account.currency && converted == null
    return copy(
        baseText = ctx.format.format(Tips.base(ctx.main)),
        tipText = tip?.let { ctx.format.format(it) }.orEmpty(),
        convertedText = converted?.let { ctx.format.format(it) },
        noRate = missingRate,
        canSave = tip != null && account != null && !missingRate && categoryId != null,
    )
}

internal fun convert(ctx: TipContext, money: Money, to: CurrencyCode): Money? =
    ctx.converter?.convert(money, to, ctx.date)?.money?.takeIf { it.isPositive }
