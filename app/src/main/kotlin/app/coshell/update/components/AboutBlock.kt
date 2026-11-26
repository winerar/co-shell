package app.coshell.update.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.MenuIcon
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
        // As the launcher shows it — lime on graphite (#755: on a light card the lime shell was hard to see; board AboutLatest).
        Image(
            painterResource(R.drawable.ic_app_tile),
            contentDescription = null,
            modifier = Modifier.size(Sizes.heroRing / 2),
        )
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
            row { Principle(DesignR.drawable.ic_account_balance_wallet, R.string.about_principle_family, R.string.about_principle_family_hint) }
            row { Principle(DesignR.drawable.ic_currency_exchange, R.string.about_principle_offline, R.string.about_principle_offline_hint) }
            row { Principle(DesignR.drawable.ic_shield_lock, R.string.about_principle_data, R.string.about_principle_data_hint) }
        }
    }
}

/**
 * A principle is read, not opened (board AboutLatest: no chevron): the whole hint is shown on as many lines as it needs.
 * As a menu row it had a chevron that did nothing and cut the hint to one line (#721).
 */
@Composable
private fun Principle(@DrawableRes icon: Int, @StringRes title: Int, @StringRes hint: Int) {
    CoShellListRow(
        title = stringResource(title),
        subtitle = stringResource(hint),
        leading = { MenuIcon(icon) },
        dense = true,
    )
}

@Composable
internal fun AboutLinks(onFeedback: () -> Unit, modifier: Modifier = Modifier) {
    CoShellRowGroup(modifier) {
        row { CoShellMenuRow(DesignR.drawable.ic_feedback, stringResource(R.string.about_feedback), onFeedback, subtitle = stringResource(R.string.feedback_menu_hint)) }
    }
}

/** Who made the app (#761, poll 2026-11-26 «Д»): two quiet centred lines at the very end of the screen, after [AboutLinks]. */
@Composable
internal fun AboutCredits(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.about_credits_ideas) + "\n" + stringResource(R.string.about_credits_dev),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}
