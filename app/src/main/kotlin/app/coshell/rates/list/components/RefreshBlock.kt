package app.coshell.rates.list.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.rates.RefreshStatus
import app.coshell.rates.list.formatRateDate
import app.coshell.rates.list.sourceLabel
import app.coshell.ui.hero.StatusCard
import app.coshell.core.designsystem.R as DesignR

/**
 * Card at the top of «Курсы валют» (mockup 11): whether rates are loaded, from where and when,
 * «Обновить сейчас» as a round button, and the refresh result under it.
 */
@Composable
internal fun RefreshBlock(lastDownload: DownloadInfo?, status: RefreshStatus, onRefresh: () -> Unit) {
    StatusCard(
        icon = if (lastDownload == null) DesignR.drawable.ic_currency_exchange else DesignR.drawable.ic_check,
        title = stringResource(if (lastDownload == null) R.string.rates_title else R.string.rates_current),
        subtitle = if (lastDownload == null) {
            stringResource(R.string.rates_never_downloaded)
        } else {
            stringResource(R.string.rates_last_download, formatRateDate(lastDownload.date), sourceLabel(lastDownload.source))
        },
        tint = LocalAmountPalette.current.income,
        trailing = {
            CoShellIconButton(onClick = onRefresh, enabled = status != RefreshStatus.Running) {
                Icon(painterResource(DesignR.drawable.ic_refresh), stringResource(R.string.rates_refresh))
            }
        },
        below = when (status) {
            RefreshStatus.Idle -> null
            RefreshStatus.Running -> ({ CoShellLinearProgress() })
            RefreshStatus.Updated -> ({ CoShellMessage(stringResource(R.string.rates_refresh_updated), isError = false) })
            RefreshStatus.AlreadyFresh -> ({ CoShellMessage(stringResource(R.string.rates_refresh_fresh), isError = false) })
            RefreshStatus.Failed -> ({ CoShellMessage(stringResource(R.string.rates_refresh_failed), isError = true) })
        },
    )
}
