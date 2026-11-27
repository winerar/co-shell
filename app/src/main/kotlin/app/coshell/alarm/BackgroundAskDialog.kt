package app.coshell.alarm

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.R as DesignR

/**
 * «Разрешить работу в фоне?» (#745, board `Set1BackgroundAsk`): asked when a reminder is turned on and once after the update.
 * «Открыть настройки» goes to the app's page in the phone's settings; both answers call [onAnswered].
 */
@Composable
fun BackgroundAskDialog(advice: BackgroundCheck.Advice, onAnswered: () -> Unit) {
    val context = LocalContext.current
    CoShellAlertDialog(
        title = stringResource(R.string.notice_background_ask_title),
        text = stringResource(if (advice == BackgroundCheck.Advice.XIAOMI) R.string.notice_background_ask_xiaomi else R.string.notice_background_ask_battery),
        confirmLabel = stringResource(R.string.notice_background_ask_open),
        onConfirm = {
            onAnswered()
            context.startActivity(BackgroundCheck.settingsIntent(context))
        },
        onDismiss = onAnswered,
        dismissLabel = stringResource(R.string.notice_background_ask_later),
        icon = DesignR.drawable.ic_bolt,
    )
}
