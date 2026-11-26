package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.ScanDuplicate
import app.coshell.transactions.edit.ScanUi
import app.coshell.transactions.edit.ReceiptAiMode
import app.coshell.ui.ai.BetaMark
import app.coshell.ui.format.formatDayMonth

/**
 * After a receipt QR (#526, board «QrFilled»): one quiet line under the amount — where the numbers came from and who
 * sold. Nothing in it is tapped.
 */
@Composable
internal fun ScanSourceLine(scan: ScanUi, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(horizontal = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(
                when {
                    scan.ai -> DesignR.drawable.ic_spark
                    scan.document -> DesignR.drawable.ic_receipt_long
                    else -> DesignR.drawable.ic_qr_code_scanner
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(Sizes.tileIcon),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            when {
                // #593, board `AiRecFilledA`: one line — the model filled the fields, the person checks them.
                scan.ai -> stringResource(R.string.scan_source_ai)
                scan.document -> stringResource(R.string.scan_source_document, scan.seller)
                // A Russian receipt QR names no shop.
                scan.seller.isBlank() -> stringResource(R.string.scan_source_plain)
                else -> stringResource(R.string.scan_source, scan.seller)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (scan.ai) BetaMark()
    }
}

/** «Этот чек уже записан» (board «QrDupB»): the operation made from it before; «Отмена» closes the sheet. */
@Composable
internal fun ScanDuplicateDialog(duplicate: ScanDuplicate, onRecordAnyway: () -> Unit, onCancel: () -> Unit) {
    val details = listOfNotNull(duplicate.category, formatDayMonth(duplicate.date), duplicate.amount).joinToString(", ")
    CoShellAlertDialog(
        title = stringResource(R.string.scan_dup_title),
        text = stringResource(R.string.scan_dup_text, details),
        confirmLabel = stringResource(R.string.scan_dup_confirm),
        onConfirm = onRecordAnyway,
        onDismiss = onCancel,
        dismissLabel = stringResource(R.string.action_cancel),
    )
}

/**
 * «Не удалось прочитать чек» (#561, board «RcUnreadA»; #593, boards `AiRecOn`/`AiRecOff`): a shared receipt of an unknown format.
 * With a model — «Прочитать с ИИ» first, then «Отправить образец» and «Ввести вручную»; no model on — «Отправить образец», «Включить ИИ»,
 * «Ввести вручную»; the model found nothing or there is no text — «Ввести вручную» and «Отправить образец». A tap outside — by hand.
 */
@Composable
internal fun ReceiptUnreadDialog(
    mode: ReceiptAiMode,
    failed: Boolean,
    onManual: () -> Unit,
    onSendSample: () -> Unit,
    onRead: () -> Unit = {},
    onEnableAi: () -> Unit = {},
) {
    val title = stringResource(R.string.receipt_unread_title)
    val manual = stringResource(R.string.scan_manual)
    val send = stringResource(R.string.receipt_unread_send)
    val icon = DesignR.drawable.ic_receipt_long
    when {
        failed || mode == ReceiptAiMode.NONE -> CoShellAlertDialog(
            title = title,
            text = stringResource(if (failed) R.string.receipt_ai_failed else R.string.receipt_unread_text),
            confirmLabel = manual, onConfirm = onManual, onDismiss = onManual,
            dismissLabel = send, onDismissClick = onSendSample, icon = icon,
        )
        mode == ReceiptAiMode.OFF -> CoShellAlertDialog(
            title = title,
            text = stringResource(R.string.receipt_ai_text_off),
            confirmLabel = send, onConfirm = onSendSample, onDismiss = onManual,
            dismissLabel = stringResource(R.string.receipt_ai_enable), onDismissClick = onEnableAi,
            extraLabel = manual, onExtra = onManual, icon = icon,
        )
        else -> CoShellAlertDialog(
            title = title,
            text = stringResource(if (mode == ReceiptAiMode.CLOUD) R.string.receipt_ai_text_cloud else R.string.receipt_ai_text_phone),
            confirmLabel = stringResource(R.string.receipt_ai_read), onConfirm = onRead, onDismiss = onManual,
            confirmIcon = DesignR.drawable.ic_spark,
            dismissLabel = send, onDismissClick = onSendSample,
            extraLabel = manual, onExtra = onManual, icon = icon,
        )
    }
}

/** «Читаю чек…» (#593, board `AiRecBusy`): a running line while the model reads; «Отменить» leaves the blank sheet. */
@Composable
internal fun ReceiptReadingDialog(cloud: Boolean, onCancel: () -> Unit) {
    CoShellAlertDialog(
        title = stringResource(R.string.receipt_ai_busy_title),
        text = stringResource(if (cloud) R.string.receipt_ai_busy_cloud else R.string.receipt_ai_busy_phone),
        confirmLabel = stringResource(R.string.action_cancel),
        onConfirm = onCancel,
        onDismiss = onCancel,
        icon = DesignR.drawable.ic_spark,
        progress = true,
    )
}
