package app.coshell.notices

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCoachCard
import app.coshell.core.designsystem.R as DesignR

/** «Напоминать вечером о тратах? · Включить · Не сейчас» (#745, board `Tip745Offer`): the card of a hint, with the answer in its two buttons. */
@Composable
fun NoticeOfferCard(onEnable: () -> Unit, onLater: () -> Unit, modifier: Modifier = Modifier) = CoShellCoachCard(
    visible = true,
    text = stringResource(R.string.notice_offer_text),
    actionLabel = stringResource(R.string.notice_offer_enable),
    onAction = onEnable,
    modifier = modifier,
    icon = DesignR.drawable.ic_lightbulb,
    secondaryLabel = stringResource(R.string.notice_offer_later),
    onSecondary = onLater,
)
