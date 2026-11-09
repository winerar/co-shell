package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.rates.RefreshStatus
import app.coshell.rates.list.formatRateDate
import app.coshell.rates.list.sourceLabel
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.R as DesignR

/** "Loaded: rates for 9 Nov, source currency-api", the "Update now" button and its result. */
@Composable
internal fun RefreshBlock(lastDownload: DownloadInfo?, status: RefreshStatus, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (lastDownload == null) {
                stringResource(R.string.rates_never_downloaded)
            } else {
                stringResource(R.string.rates_last_download, formatRateDate(lastDownload.date), sourceLabel(lastDownload.source))
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CoShellOutlinedButton(onClick = onRefresh, enabled = status != RefreshStatus.Running) {
            Icon(painterResource(DesignR.drawable.ic_refresh), null, Modifier.size(ButtonDefaults.IconSize))
            Text(stringResource(R.string.rates_refresh), Modifier.padding(start = ButtonDefaults.IconSpacing))
        }
        when (status) {
            RefreshStatus.Idle -> Unit
            RefreshStatus.Running -> LinearProgressIndicator(Modifier.fillMaxWidth())
            RefreshStatus.Updated -> CoShellMessage(stringResource(R.string.rates_refresh_updated), isError = false)
            RefreshStatus.AlreadyFresh -> CoShellMessage(stringResource(R.string.rates_refresh_fresh), isError = false)
            RefreshStatus.Failed -> CoShellMessage(stringResource(R.string.rates_refresh_failed), isError = true)
        }
    }
}
