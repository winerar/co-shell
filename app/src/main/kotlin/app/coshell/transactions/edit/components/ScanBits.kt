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
                scan.document -> stringResource(R.string.scan_source_document, scan.seller)
                // A Russian receipt QR names no shop.
                scan.seller.isBlank() -> stringResource(R.string.scan_source_plain)
                else -> stringResource(R.string.scan_source, scan.seller)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
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
 * «Не удалось прочитать чек» (#561, board «RcUnreadA»): a shared receipt of an unknown format — «Ввести вручную» or «Отправить образец»
 * (a feedback with the file attached, so the format can be added). A tap outside — by hand.
 */
@Composable
internal fun ReceiptUnreadDialog(onManual: () -> Unit, onSendSample: () -> Unit) {
    CoShellAlertDialog(
        title = stringResource(R.string.receipt_unread_title),
        text = stringResource(R.string.receipt_unread_text),
        confirmLabel = stringResource(R.string.scan_manual), onConfirm = onManual, onDismiss = onManual,
        dismissLabel = stringResource(R.string.receipt_unread_send), onDismissClick = onSendSample, icon = DesignR.drawable.ic_receipt_long,
    )
}
