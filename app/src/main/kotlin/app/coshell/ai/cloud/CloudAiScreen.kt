package app.coshell.ai.cloud

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.ai.AiFactRow
import app.coshell.ui.ai.AiStatusCard
import app.coshell.ui.ai.BetaMark
import app.coshell.ui.top.ScreenTopBar

/** Everything the screen can ask for; defaults make previews short. */
data class CloudAiActions(
    val onBack: () -> Unit = {},
    val onToggle: (Boolean) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)

/**
 * «Ещё → Настройки → Умные функции → Облачный разбор» (#635; boards `CloudOff`, `CloudOn`, `CloudNoLogin`, `CloudConsent`): the state card, one
 * switch, what is sent and what is not (off) or where it works (on). Turning it on asks first.
 */
@Composable
fun CloudAiScreen(state: CloudAiUiState, actions: CloudAiActions) {
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.cloud_title), onBack = actions.onBack, mark = { BetaMark() }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).verticalScroll(rememberScrollState())
                .padding(top = padding.calculateTopPadding()).padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            StatusCard(state)
            CoShellRowGroup {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.cloud_title),
                        checked = state.stage == CloudAiStage.ON,
                        onCheckedChange = actions.onToggle,
                        enabled = state.stage != CloudAiStage.NO_LOGIN,
                    )
                }
            }
            if (state.stage == CloudAiStage.ON) WhereItWorks() else Facts()
        }
    }
    if (state.askConsent) {
        CoShellAlertDialog(
            title = stringResource(R.string.cloud_consent_title),
            text = stringResource(R.string.cloud_consent_text),
            confirmLabel = stringResource(R.string.cloud_consent_confirm),
            onConfirm = actions.onConfirm,
            onDismiss = actions.onDismiss,
            dismissLabel = stringResource(R.string.cloud_consent_dismiss),
            icon = DesignR.drawable.ic_cloud_download,
        )
    }
}

@Composable
private fun StatusCard(state: CloudAiUiState) {
    val on = state.stage == CloudAiStage.ON
    val left = state.left
    val limit = state.limit
    val text = when {
        !on -> stringResource(R.string.cloud_off_text)
        left != null && limit != null -> stringResource(R.string.cloud_on_text_left, left, limit)
        else -> stringResource(R.string.cloud_on_text)
    }
    AiStatusCard(stringResource(if (on) R.string.cloud_on_title else R.string.cloud_off_title), text) {
        when (state.stage) {
            CloudAiStage.OFF -> CoShellStatusChip(stringResource(R.string.cloud_off_chip), StatusTone.Done)
            CloudAiStage.ON -> CoShellStatusChip(stringResource(R.string.cloud_on_chip), StatusTone.Done)
            CloudAiStage.NO_LOGIN -> CoShellStatusChip(stringResource(R.string.cloud_nologin_chip), StatusTone.Work)
        }
    }
}

@Composable
private fun Facts() = CoShellRowGroup {
    row { Fact(DesignR.drawable.ic_description, R.string.cloud_fact_sent, R.string.cloud_fact_sent_text) }
    row { Fact(DesignR.drawable.ic_account_balance_wallet, R.string.cloud_fact_kept, R.string.cloud_fact_kept_text) }
    row { Fact(DesignR.drawable.ic_cloud_download, R.string.cloud_fact_net, R.string.cloud_fact_net_text) }
}

@Composable
private fun WhereItWorks() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellSectionLabel(stringResource(R.string.ai_where).uppercase(), Modifier.padding(horizontal = Spacing.xs))
        CoShellRowGroup {
            row { Fact(DesignR.drawable.ic_description, R.string.cloud_use_notes, R.string.cloud_use_notes_text) }
            row { Fact(DesignR.drawable.ic_description, R.string.ai_use_receipts, R.string.ai_use_receipts_text) }
            row { Fact(DesignR.drawable.ic_spark, R.string.ai_use_category, R.string.ai_use_category_text) }
        }
    }
}

@Composable
private fun Fact(icon: Int, @StringRes title: Int, @StringRes text: Int) = AiFactRow(icon, stringResource(title), stringResource(text))
