package app.coshell.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.BuildConfig
import app.coshell.R
import app.coshell.access.AccessViewModel
import app.coshell.access.InviteRow
import app.coshell.access.inviteRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.hints.HintsSettingsViewModel
import app.coshell.language.AppLanguage
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

/**
 * «Настройки → Помощь» (poll 2026-11-28 «А»: a group screen like the others, was a card at the bottom of «Настройки»; board Set1Help):
 * hints on/off with «Показать подсказки заново» (#598 «Б»), the tour, gestures; «Код приглашения» — only in builds with the gate
 * (#671); «Политика конфиденциальности» — only in builds that know the site (#652).
 */
data class HelpState(
    val hints: Boolean = true,
    val invite: InviteRow? = null,
    val policy: Boolean = false,
)

data class HelpActions(
    val onBack: () -> Unit = {},
    val onHints: (Boolean) -> Unit = {},
    val onHintsReset: () -> Unit = {},
    val onTour: () -> Unit = {},
    val onGestures: () -> Unit = {},
    val onInvite: () -> Unit = {},
    val onPolicy: () -> Unit = {},
)

@Composable
fun HelpSettingsRoute(
    onBack: () -> Unit,
    onTour: () -> Unit,
    onGestures: () -> Unit,
    hints: HintsSettingsViewModel = hiltViewModel(),
    access: AccessViewModel = hiltViewModel(),
) {
    val hintsOn by hints.enabled.collectAsStateWithLifecycle()
    val accessState by access.state.collectAsStateWithLifecycle()
    val uris = LocalUriHandler.current
    val policyUrl = remember { PolicyLink.of(BuildConfig.SITE_URL, AppLanguage.current()) }
    HelpSettingsScreen(
        HelpState(hintsOn, accessState.inviteRow(), policyUrl != null),
        HelpActions(
            onBack = onBack,
            onHints = hints::setEnabled,
            onHintsReset = hints::reset,
            onTour = onTour,
            onGestures = onGestures,
            onInvite = access::changeCode,
            onPolicy = { policyUrl?.let { url -> runCatching { uris.openUri(url) } } },
        ),
    )
}

@Composable
fun HelpSettingsScreen(state: HelpState, actions: HelpActions) {
    GroupScaffold(R.string.settings_help_title, actions.onBack) {
        item { SectionTitle(stringResource(R.string.hints_title)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    Column {
                        CoShellSwitchRow(
                            title = stringResource(R.string.hints_title),
                            subtitle = stringResource(if (state.hints) R.string.hints_on else R.string.hints_off),
                            checked = state.hints, onCheckedChange = actions.onHints,
                        )
                        CoShellOutlinedButton(
                            onClick = actions.onHintsReset,
                            modifier = Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.m),
                        ) {
                            Icon(painterResource(DesignR.drawable.ic_refresh), contentDescription = null)
                            Text(stringResource(R.string.hints_reset_title), modifier = Modifier.padding(start = Spacing.s))
                        }
                    }
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_help_more)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_lightbulb, stringResource(R.string.tour_title), actions.onTour, subtitle = stringResource(R.string.tour_hint)) }
                row { CoShellMenuRow(DesignR.drawable.ic_info, stringResource(R.string.gestures_title), actions.onGestures, subtitle = stringResource(R.string.gestures_hint)) }
                state.invite?.let { invite ->
                    row {
                        // A revoked code says so in the warning colour (#671, board Inv671ARevoked).
                        CoShellListRow(
                            title = { Text(stringResource(R.string.settings_invite_title)) },
                            leading = { MenuIcon(DesignR.drawable.ic_lock) },
                            supporting = {
                                Text(
                                    if (invite.revoked) stringResource(R.string.settings_invite_revoked) else stringResource(R.string.settings_invite_for, invite.label),
                                    color = if (invite.revoked) LocalStatusPalette.current.warning else Color.Unspecified,
                                )
                            },
                            trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
                            onClick = actions.onInvite,
                            dense = true,
                        )
                    }
                }
                if (state.policy) {
                    row {
                        CoShellMenuRow(
                            DesignR.drawable.ic_description, stringResource(R.string.settings_policy_title), actions.onPolicy,
                            subtitle = stringResource(R.string.settings_policy_hint),
                        )
                    }
                }
            }
        }
    }
}
