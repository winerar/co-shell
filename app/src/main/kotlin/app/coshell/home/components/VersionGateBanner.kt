package app.coshell.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.coShellButtonColors
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * The newer version the family needs (#334): [version] and [sizeMb] come from the last update check,
 * `null` while it is not known yet.
 */
data class VersionGateBannerState(val version: String? = null, val sizeMb: String? = null)

/**
 * Entry is closed until the update (board `Fb334A`): the same warning card as the sync banner — why,
 * that looking still works, and «Обновить».
 */
@Composable
fun VersionGateBanner(state: VersionGateBannerState, onUpdate: () -> Unit, modifier: Modifier = Modifier) {
    val warning = LocalStatusPalette.current.warning
    CoShellCard(modifier = modifier.fillMaxWidth(), tone = CardTone.Warning) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Icon(painterResource(DesignR.drawable.ic_download), contentDescription = null, tint = warning, modifier = Modifier.size(Sizes.tileIcon))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        state.version?.let { stringResource(R.string.gate_banner_title, it) } ?: stringResource(R.string.gate_banner_title_any),
                        style = MaterialTheme.typography.titleSmall,
                        color = warning,
                    )
                    Text(stringResource(R.string.gate_banner_text), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CoShellButton(
                    onClick = onUpdate,
                    colors = coShellButtonColors(warning, MaterialTheme.colorScheme.surface),
                ) {
                    Text(state.sizeMb?.let { stringResource(R.string.gate_banner_update_size, it) } ?: stringResource(R.string.gate_banner_update))
                }
            }
        }
    }
}
