package app.coshell.ai

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.components.StatusTone
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.ai.BetaMark
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.top.ScreenTopBar
import java.text.NumberFormat

/** Everything the screen can ask for; defaults make previews short. */
data class AiAddonActions(
    val onBack: () -> Unit = {},
    val onEnable: () -> Unit = {},
    val onConfirmDownload: () -> Unit = {},
    val onAskDismiss: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onDeleteModel: () -> Unit = {},
)

/**
 * «Ещё → Настройки → Умные функции → ИИ на телефоне» (#587; boards `AiOff`, `AiStep1`, `AiStep2`, `AiOn`, `AiData`): the one main card with
 * the state and a bar, under it the facts, the two steps or where it works; the action sits full width at the bottom.
 */
@Composable
fun AiAddonScreen(state: AiAddonUiState, actions: AiAddonActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.ai_title), onBack = actions.onBack, mark = { BetaMark() }) },
        bottomBar = { MainAction(state, actions, Modifier.aboveBottomBars().padding(horizontal = Spacing.l, vertical = Spacing.m)) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).verticalScroll(rememberScrollState())
                .padding(top = padding.calculateTopPadding()).padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            StatusCard(state)
            when (state.stage) {
                AiAddonStage.OFF -> Facts()
                AiAddonStage.STEP_ADDON -> Steps(addon = StepState.NOW, model = StepState.WAIT)
                AiAddonStage.STEP_MODEL -> Steps(addon = StepState.DONE, model = StepState.NOW)
                AiAddonStage.ON -> WhereItWorks()
                AiAddonStage.UNSUPPORTED -> Facts()
            }
            state.problem?.let { CoShellMessage(stringResource(it.messageRes()), isError = true) }
        }
    }
    if (state.askData) {
        CoShellAlertDialog(
            title = stringResource(R.string.ai_data_title),
            text = stringResource(R.string.ai_data_text),
            confirmLabel = stringResource(R.string.ai_data_confirm),
            onConfirm = actions.onConfirmDownload,
            onDismiss = actions.onAskDismiss,
            dismissLabel = stringResource(R.string.action_cancel),
            icon = DesignR.drawable.ic_cloud_download,
        )
    }
}

@Composable
private fun StatusCard(state: AiAddonUiState) {
    val (title, text) = when (state.stage) {
        AiAddonStage.OFF -> R.string.ai_off_title to R.string.ai_off_text
        AiAddonStage.STEP_ADDON -> R.string.ai_step1_title to R.string.ai_step1_text
        AiAddonStage.STEP_MODEL -> R.string.ai_step2_title to R.string.ai_step2_text
        AiAddonStage.ON -> R.string.ai_on_title to R.string.ai_on_text
        AiAddonStage.UNSUPPORTED -> R.string.ai_weak_title to R.string.ai_weak_text
    }
    CoShellCard(Modifier.fillMaxWidth(), tone = CardTone.Hero) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            IconBadge(DesignR.drawable.ic_spark, LocalHeroPalette.current.amount)
            Text(stringResource(title), style = MaterialTheme.typography.headlineSmall, color = LocalHeroPalette.current.content)
            Text(
                if (state.stage == AiAddonStage.UNSUPPORTED) stringResource(text, state.memoryGb) else stringResource(text),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalHeroPalette.current.hint,
            )
            when (state.stage) {
                AiAddonStage.OFF -> CoShellStatusChip(stringResource(R.string.ai_off_chip), StatusTone.Done)
                AiAddonStage.STEP_ADDON -> if (state.progress == null) CoShellStatusChip(stringResource(R.string.ai_step1_chip), StatusTone.Work) else Bar(state)
                AiAddonStage.STEP_MODEL -> Bar(state)
                AiAddonStage.ON -> CoShellStatusChip(stringResource(R.string.ai_on_chip), StatusTone.Done)
                AiAddonStage.UNSUPPORTED -> Unit
            }
        }
    }
}

@Composable
private fun Bar(state: AiAddonUiState) {
    val fraction = state.progress
    if (fraction == null) CoShellLinearProgress() else CoShellLinearProgress({ fraction })
    if (state.stage == AiAddonStage.STEP_MODEL && state.totalMb > 0) {
        val number = NumberFormat.getIntegerInstance()
        Text(
            stringResource(R.string.ai_progress, number.format(state.doneMb), number.format(state.totalMb), ((fraction ?: 0f) * 100).toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = LocalHeroPalette.current.hint,
        )
    }
}

@Composable
private fun Facts() = CoShellRowGroup {
    row { FactRow(DesignR.drawable.ic_cloud_download, R.string.ai_fact_size, R.string.ai_fact_size_text) }
    row { FactRow(DesignR.drawable.ic_memory, R.string.ai_fact_memory, R.string.ai_fact_memory_text) }
    row { FactRow(DesignR.drawable.ic_lock, R.string.ai_fact_privacy, R.string.ai_fact_privacy_text) }
}

private enum class StepState { DONE, NOW, WAIT }

@Composable
private fun Steps(addon: StepState, model: StepState) = CoShellRowGroup {
    row { StepRow(R.string.ai_addon, addon, if (addon == StepState.DONE) R.string.ai_addon_done else R.string.ai_addon_now) }
    row { StepRow(R.string.ai_model, model, if (model == StepState.NOW) R.string.ai_model_now else R.string.ai_model_wait) }
}

@Composable
private fun StepRow(@StringRes title: Int, state: StepState, @StringRes subtitle: Int) {
    val icon = when (state) {
        StepState.DONE -> DesignR.drawable.ic_check
        StepState.NOW -> DesignR.drawable.ic_download
        StepState.WAIT -> DesignR.drawable.ic_cloud_download
    }
    val color = if (state == StepState.WAIT) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
    CoShellListRow(title = stringResource(title), subtitle = stringResource(subtitle), leading = { MenuIcon(icon, color) })
}

@Composable
private fun FactRow(icon: Int, @StringRes title: Int, @StringRes text: Int) =
    CoShellListRow(title = stringResource(title), subtitle = stringResource(text), leading = { MenuIcon(icon, MaterialTheme.colorScheme.secondary) })

@Composable
private fun WhereItWorks() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellSectionLabel(stringResource(R.string.ai_where).uppercase(), Modifier.padding(horizontal = Spacing.xs))
        CoShellRowGroup {
            row { FactRow(DesignR.drawable.ic_description, R.string.ai_use_receipts, R.string.ai_use_receipts_text) }
            row { FactRow(DesignR.drawable.ic_description, R.string.ai_use_notes, R.string.ai_use_notes_text) }
            row { FactRow(DesignR.drawable.ic_spark, R.string.ai_use_category, R.string.ai_use_category_text) }
        }
        Text(
            stringResource(R.string.ai_on_note),
            Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MainAction(state: AiAddonUiState, actions: AiAddonActions, modifier: Modifier) {
    when (state.stage) {
        AiAddonStage.OFF -> CoShellButton(actions.onEnable, modifier.fillMaxWidth()) {
            Icon(painterResource(DesignR.drawable.ic_cloud_download), contentDescription = null, tint = Color.Unspecified)
            Text(stringResource(R.string.ai_enable))
        }
        AiAddonStage.STEP_ADDON, AiAddonStage.STEP_MODEL ->
            CoShellOutlinedButton(actions.onCancel, modifier.fillMaxWidth()) { Text(stringResource(R.string.ai_cancel)) }
        AiAddonStage.UNSUPPORTED -> Unit
        AiAddonStage.ON -> CoShellOutlinedButton(actions.onDeleteModel, modifier.fillMaxWidth()) { Text(stringResource(R.string.ai_delete_model)) }
    }
}

@StringRes
private fun AiAddonProblem.messageRes(): Int = when (this) {
    AiAddonProblem.NO_ADDON_FILE -> R.string.ai_problem_no_file
    AiAddonProblem.INSTALL_CANCELLED -> R.string.ai_problem_cancelled
    AiAddonProblem.INSTALL_FAILED -> R.string.ai_problem_install
    AiAddonProblem.DOWNLOAD_FAILED -> R.string.ai_problem_download
}
