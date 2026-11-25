package app.coshell.privacy.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.privacy.RelockDelay
import app.coshell.settings.GroupScaffold
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

data class PrivacyActions(
    /** «Вход по PIN»: on → choose a PIN, off → confirm the current one. */
    val onPin: (Boolean) -> Unit = {},
    val onChangePin: () -> Unit = {},
    val change: ((PrivacyOptions) -> PrivacyOptions) -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * «Ещё → Конфиденциальность» (#145, boards `Priv145Settings`, `Priv145SettingsOff`): Вход · Суммы · Экран, one screen
 * without scrolling. [biometricAvailable] — the phone has a fingerprint or face; without a PIN the row is dimmed.
 */
@Composable
fun PrivacyScreen(options: PrivacyOptions, biometricAvailable: Boolean, actions: PrivacyActions) {
    val pin = options.pinEnabled
    GroupScaffold(R.string.privacy_title, actions.onBack) {
        item { SectionTitle(stringResource(R.string.privacy_section_entry)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.privacy_pin),
                        subtitle = if (pin) stringResource(R.string.privacy_pin_on) else null,
                        checked = pin,
                        onCheckedChange = actions.onPin,
                    )
                }
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.privacy_biometric),
                        subtitle = stringResource(if (biometricAvailable) R.string.privacy_biometric_hint else R.string.privacy_biometric_none),
                        checked = pin && options.biometric && biometricAvailable,
                        onCheckedChange = { v -> actions.change { it.copy(biometric = v) } },
                        enabled = pin && biometricAvailable,
                    )
                }
                if (pin) {
                    row {
                        CoShellListRow(
                            title = stringResource(R.string.privacy_change_pin),
                            onClick = actions.onChangePin,
                            trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
                        )
                    }
                }
            }
        }
        if (pin) item { RelockCard(options.relock) { r -> actions.change { it.copy(relock = r) } } }
        item { SectionTitle(stringResource(R.string.privacy_section_amounts)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
                row { Switch(R.string.privacy_hide_launch, R.string.privacy_hide_launch_hint, options.hideOnLaunch) { v -> actions.change { it.copy(hideOnLaunch = v) } } }
                row { Switch(R.string.privacy_face_down, R.string.privacy_face_down_hint, options.hideFaceDown) { v -> actions.change { it.copy(hideFaceDown = v) } } }
            }
        }
        item { SectionTitle(stringResource(R.string.privacy_section_screen)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { Switch(R.string.privacy_recents, R.string.privacy_recents_hint, options.hideInRecents) { v -> actions.change { it.copy(hideInRecents = v) } } }
                row { Switch(R.string.privacy_screenshots, R.string.privacy_screenshots_hint, options.blockScreenshots) { v -> actions.change { it.copy(blockScreenshots = v) } } }
            }
        }
    }
}

@Composable
private fun Switch(title: Int, subtitle: Int, checked: Boolean, onChange: (Boolean) -> Unit) =
    CoShellSwitchRow(title = stringResource(title), subtitle = stringResource(subtitle), checked = checked, onCheckedChange = onChange)

/** «Блокировать после выхода»: Сразу · 1 мин · 5 мин. */
@Composable
private fun RelockCard(relock: RelockDelay, onSelect: (RelockDelay) -> Unit) {
    val all = RelockDelay.entries
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
        row {
            Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m)) {
                Text(stringResource(R.string.privacy_relock), style = MaterialTheme.typography.bodyLarge)
                CoShellSegmented(
                    labels = listOf(stringResource(R.string.privacy_relock_now), stringResource(R.string.privacy_relock_1), stringResource(R.string.privacy_relock_5)),
                    selected = all.indexOf(relock),
                    onSelect = { onSelect(all[it]) },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.m),
                )
                Text(
                    stringResource(if (relock == RelockDelay.NOW) R.string.privacy_relock_now_hint else R.string.privacy_relock_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.s),
                )
            }
        }
    }
}
