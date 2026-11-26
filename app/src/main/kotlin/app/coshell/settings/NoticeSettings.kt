package app.coshell.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import app.coshell.recurring.notify.PaymentNotices
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ChoiceOption
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.notices.NoticeKindsViewModel
import app.coshell.notices.NoticeScheduler
import app.coshell.recurring.notify.RecurringPaymentsScheduler
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

/** «Уведомления»: payment reminders and their time. */
@Composable
fun NoticeSettingsRoute(
    onBack: () -> Unit,
    viewModel: ReminderSettingsViewModel = hiltViewModel(),
    kindsViewModel: NoticeKindsViewModel = hiltViewModel(),
    screens: ScreenOptionsViewModel = hiltViewModel(),
    accounts: SettingsAccountsViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val hour by viewModel.hour.collectAsStateWithLifecycle()
    val daysBefore by viewModel.daysBefore.collectAsStateWithLifecycle()
    val kinds by kindsViewModel.enabled.collectAsStateWithLifecycle()
    val noticeHour by kindsViewModel.hour.collectAsStateWithLifecycle()
    val options by screens.options.collectAsStateWithLifecycle()
    val main by accounts.mainCurrency.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    // Android 13+: the notification is asked for when «Трата партнёра» is turned on.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    NoticeSettingsScreen(
        remindersEnabled = enabled,
        onRemindersChange = viewModel::setEnabled,
        hour = hour,
        onHourChange = { h -> viewModel.setHour(h, after = { RecurringPaymentsScheduler.schedule(context, replace = true) }) },
        daysBefore = daysBefore,
        onDaysBeforeChange = viewModel::setDaysBefore,
        partnerSpend = options.partnerSpend,
        onPartnerSpend = { on ->
            screens.change { it.copy(partnerSpend = on) }
            if (on && !PaymentNotices.canNotify(context)) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        partnerMin = options.partnerSpendMin,
        onPartnerMin = { v -> screens.change { it.copy(partnerSpendMin = v) } },
        mainCurrency = main,
        onBack = onBack,
        kinds = kinds,
        onKind = { kind, on ->
            kindsViewModel.setEnabled(kind, on)
            if (on && !PaymentNotices.canNotify(context)) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        noticeHour = noticeHour,
        onNoticeHour = { h -> kindsViewModel.setHour(h, after = { NoticeScheduler.schedule(context, replace = true) }) },
    )
}

@Composable
fun NoticeSettingsScreen(
    remindersEnabled: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    hour: Int,
    onHourChange: (Int) -> Unit,
    daysBefore: Int,
    onDaysBeforeChange: (Int) -> Unit,
    partnerSpend: Boolean,
    onPartnerSpend: (Boolean) -> Unit,
    partnerMin: Int,
    onPartnerMin: (Int) -> Unit,
    mainCurrency: String,
    onBack: () -> Unit,
    kinds: Set<NoticeKind> = emptySet(),
    onKind: (NoticeKind, Boolean) -> Unit = { _, _ -> },
    noticeHour: Int = NoticeSettings.DEFAULT_HOUR,
    onNoticeHour: (Int) -> Unit = {},
) {
    var pickingMin by rememberSaveable { mutableStateOf(false) }
    GroupScaffold(R.string.settings_notices_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_notices_payments)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(DesignR.drawable.ic_refresh) },
                        title = stringResource(R.string.reminders_title),
                        subtitle = stringResource(R.string.reminders_hint),
                        checked = remindersEnabled,
                        onCheckedChange = onRemindersChange,
                    )
                }
            }
        }
        item {
            PickCard(
                title = stringResource(R.string.settings_notices_time),
                labels = ReminderSettings.HOURS.map { stringResource(R.string.settings_notices_hour, it) },
                selected = ReminderSettings.HOURS.indexOf(hour).coerceAtLeast(0),
                onSelect = { onHourChange(ReminderSettings.HOURS[it]) },
            )
        }
        item {
            PickCard(
                title = stringResource(R.string.settings_notices_before),
                labels = ReminderSettings.DAYS_BEFORE.map { if (it == 0) stringResource(R.string.settings_notices_same_day) else stringResource(R.string.settings_notices_days, it) },
                selected = ReminderSettings.DAYS_BEFORE.indexOf(daysBefore).coerceAtLeast(0),
                onSelect = { onDaysBeforeChange(ReminderSettings.DAYS_BEFORE[it]) },
            )
        }
        noticeKindGroups(kinds, onKind, noticeHour, onNoticeHour)
        item { CoShellHint(stringResource(R.string.notice_limit_note), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
        item { SectionTitle(stringResource(R.string.settings_notices_family)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(DesignR.drawable.ic_group) },
                        title = stringResource(R.string.settings_notices_partner),
                        subtitle = stringResource(R.string.settings_notices_partner_hint),
                        checked = partnerSpend,
                        onCheckedChange = onPartnerSpend,
                    )
                }
                if (partnerSpend) {
                    row {
                        CoShellMenuRow(
                            DesignR.drawable.ic_payments,
                            stringResource(R.string.settings_notices_min), { pickingMin = true },
                            subtitle = if (partnerMin == 0) stringResource(R.string.settings_notices_min_any) else "$partnerMin $mainCurrency",
                        )
                    }
                }
            }
        }
        item { CoShellHint(stringResource(R.string.settings_notices_partner_note), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
    }
    if (pickingMin) {
        ChoiceSheet(
            title = stringResource(R.string.settings_notices_min),
            options = ScreenOptions.PARTNER_MINIMUMS.map { ChoiceOption(it, if (it == 0) stringResource(R.string.settings_notices_min_any) else "$it $mainCurrency") },
            selected = partnerMin,
            onPick = { v -> onPartnerMin(v); pickingMin = false },
            onDismiss = { pickingMin = false },
        )
    }
}
