package app.coshell.transactions.offer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.components.CoShellCoachCard
import app.coshell.core.designsystem.R as DesignR

/** «Похоже на ежемесячный платёж … Сделать регулярным? · Не сейчас» (#789, poll «А», board `Smart789Coach`): a hint card at the top of «Операций». */
@Composable
internal fun RecurringOfferCard(offer: RecurringOffer, onAccept: () -> Unit, onLater: () -> Unit, modifier: Modifier = Modifier) = CoShellCoachCard(
    visible = true,
    text = if (offer.weekly) stringResource(R.string.recurring_offer_weekly, offer.name, shownAmount(offer.amount))
    else stringResource(R.string.recurring_offer_monthly, offer.name, shownAmount(offer.amount), offer.day),
    actionLabel = stringResource(R.string.recurring_offer_accept),
    onAction = onAccept,
    modifier = modifier,
    icon = DesignR.drawable.ic_lightbulb,
    secondaryLabel = stringResource(R.string.recurring_offer_later),
    onSecondary = onLater,
)
