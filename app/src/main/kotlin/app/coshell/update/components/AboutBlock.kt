package app.coshell.update.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.R as DesignR

/**
 * «Ещё → О приложении» (#440, poll 2026-11-23, А; boards AboutLatest, AboutUpdate): the icon, the name and the version on top
 * (instead of the ring while nothing is happening), then [AboutBlock] — two sentences and three principles — and [AboutLinks].
 */
@Composable
internal fun AboutHead(versionName: String, build: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellCard {
            Image(
                painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.padding(Spacing.s).size(Sizes.heroRing / 2),
            )
        }
        Text(stringResource(R.string.about_name), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = Spacing.s))
        Text(
            stringResource(R.string.version_label, versionName, build),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun AboutBlock(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellCard {
            Text(stringResource(R.string.about_description), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.m))
        }
        CoShellSectionLabel(stringResource(R.string.about_principles))
        CoShellRowGroup {
            row { CoShellMenuRow(DesignR.drawable.ic_account_balance_wallet, stringResource(R.string.about_principle_family), {}, subtitle = stringResource(R.string.about_principle_family_hint)) }
            row { CoShellMenuRow(DesignR.drawable.ic_currency_exchange, stringResource(R.string.about_principle_offline), {}, subtitle = stringResource(R.string.about_principle_offline_hint)) }
            row { CoShellMenuRow(DesignR.drawable.ic_shield_lock, stringResource(R.string.about_principle_data), {}, subtitle = stringResource(R.string.about_principle_data_hint)) }
        }
    }
}

@Composable
internal fun AboutLinks(onFeedback: () -> Unit, modifier: Modifier = Modifier) {
    CoShellRowGroup(modifier) {
        row { CoShellMenuRow(DesignR.drawable.ic_feedback, stringResource(R.string.about_feedback), onFeedback, subtitle = stringResource(R.string.feedback_menu_hint)) }
    }
}
