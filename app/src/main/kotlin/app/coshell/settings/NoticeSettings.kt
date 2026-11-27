package app.coshell.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import app.coshell.recurring.notify.PaymentNotices
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyListScope
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.coshell.alarm.BackgroundAskDialog
import app.coshell.alarm.BackgroundAskViewModel
import app.coshell.alarm.BackgroundCheck
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
    backgroundAsk: BackgroundAskViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val time by viewModel.time.collectAsStateWithLifecycle()
    val daysBefore by viewModel.daysBefore.collectAsStateWithLifecycle()
    val kinds by kindsViewModel.enabled.collectAsStateWithLifecycle()
    val noticeTime by kindsViewModel.time.collectAsStateWithLifecycle()
    val options by screens.options.collectAsStateWithLifecycle()
    val main by accounts.mainCurrency.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    // Android 13+: the notification is asked for when «Трата партнёра» is turned on.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    // Read again on every return: the person may have just changed it in the phone's settings.
    var background by remember { mutableStateOf(BackgroundCheck.Advice.NONE) }
    LifecycleResumeEffect(Unit) {
        background = BackgroundCheck.advice(context)
        onPauseOrDispose {}
    }
    // A reminder was just turned on: «Разрешить работу в фоне?» (#745).
    var asking by rememberSaveable { mutableStateOf(false) }
    val anythingOn = enabled || kinds.isNotEmpty() || options.partnerSpend
    val turnedOn = { if (BackgroundCheck.askOnEnable(background, anythingOn)) asking = true }
    NoticeSettingsScreen(
        remindersEnabled = enabled,
        onRemindersChange = { on -> if (on) turnedOn(); viewModel.setEnabled(on) },
        time = time,
        onTimeChange = { t -> viewModel.setTime(t, after = { RecurringPaymentsScheduler.schedule(context, replace = true) }) },
        daysBefore = daysBefore,
        onDaysBeforeChange = viewModel::setDaysBefore,
        partnerSpend = options.partnerSpend,
        onPartnerSpend = { on ->
            if (on) turnedOn()
            screens.change { it.copy(partnerSpend = on) }
            if (on && !PaymentNotices.canNotify(context)) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        partnerMin = options.partnerSpendMin,
        onPartnerMin = { v -> screens.change { it.copy(partnerSpendMin = v) } },
        mainCurrency = main,
        onBack = onBack,
        kinds = kinds,
        onKind = { kind, on ->
            if (on) turnedOn()
            kindsViewModel.setEnabled(kind, on)
            if (on && !PaymentNotices.canNotify(context)) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        noticeTime = noticeTime,
        onNoticeTime = { t -> kindsViewModel.setTime(t, after = { NoticeScheduler.schedule(context, replace = true) }) },
        background = background,
        onBackground = { context.startActivity(BackgroundCheck.settingsIntent(context)) },
    )
    if (asking) BackgroundAskDialog(background) { asking = false; backgroundAsk.answered() }
}

@Composable
fun NoticeSettingsScreen(
    remindersEnabled: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    time: Int,
    onTimeChange: (Int) -> Unit,
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
    noticeTime: Int = NoticeSettings.DEFAULT_TIME,
    onNoticeTime: (Int) -> Unit = {},
    background: BackgroundCheck.Advice = BackgroundCheck.Advice.NONE,
    onBackground: () -> Unit = {},
) {
    var pickingMin by rememberSaveable { mutableStateOf(false) }
    GroupScaffold(R.string.settings_notices_title, onBack) {
        if (background != BackgroundCheck.Advice.NONE && (remindersEnabled || kinds.isNotEmpty() || partnerSpend)) backgroundRow(background, onBackground)
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
        item { TimePickCard(stringResource(R.string.settings_notices_time), time, ReminderSettings.TIMES, onTimeChange) }
        item { DaysPickCard(daysBefore, onDaysBeforeChange) }
        noticeKindGroups(kinds, onKind, noticeTime, onNoticeTime)
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

/** #745: the phone limits the app in the background — the reminders would not come while it is closed (board `Set1Notices`). */
private fun LazyListScope.backgroundRow(advice: BackgroundCheck.Advice, onOpen: () -> Unit) {
    item { SectionTitle(stringResource(R.string.notice_background_section)) }
    item {
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
            row {
                CoShellMenuRow(
                    DesignR.drawable.ic_bolt,
                    stringResource(R.string.notice_background_title), onOpen,
                    subtitle = stringResource(if (advice == BackgroundCheck.Advice.XIAOMI) R.string.notice_background_xiaomi else R.string.notice_background_battery),
                )
            }
        }
    }
    item { CoShellHint(stringResource(if (advice == BackgroundCheck.Advice.XIAOMI) R.string.notice_background_xiaomi_note else R.string.notice_background_note), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
}
