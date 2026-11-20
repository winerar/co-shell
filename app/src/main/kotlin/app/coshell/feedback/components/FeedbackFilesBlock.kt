package app.coshell.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.feedback.FeedbackKind
import app.coshell.feedback.files.FeedbackFile
import app.coshell.core.designsystem.R as DesignR

/**
 * «Файлы» of the feedback form (#561, poll: board «RcFormA»): a card with one row per file (icon, name, type and size, «✕»)
 * and «+ Прикрепить» in its caption. For «Формат чека» with no file yet — the «Приложите чек · Выбрать» card («RcKindEmpty»).
 */
@Composable
internal fun FeedbackFilesBlock(
    kind: FeedbackKind,
    files: List<FeedbackFile>,
    canAttach: Boolean,
    fileError: Boolean,
    onAttach: () -> Unit,
    onRemove: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        if (files.isEmpty() && kind == FeedbackKind.RECEIPT_FORMAT) {
            CoShellCard {
                CoShellListRow(
                    title = stringResource(R.string.feedback_receipt_empty_title),
                    subtitle = stringResource(R.string.feedback_receipt_empty_text),
                    leading = { IconBadge(DesignR.drawable.ic_upload, null) },
                    trailing = { CoShellTextButton(onClick = onAttach) { Text(stringResource(R.string.feedback_receipt_choose)) } },
                    onClick = onAttach,
                )
            }
        } else {
            CoShellFormSection(
                title = stringResource(R.string.feedback_files),
                trailing = if (canAttach) ({ CoShellSectionLink(stringResource(R.string.feedback_attach), onAttach) }) else null,
            ) {
                files.forEach { file -> FileRow(file, onRemove) }
            }
        }
        val hint = if (fileError) R.string.feedback_file_error else R.string.feedback_files_hint
        Text(
            stringResource(hint),
            style = MaterialTheme.typography.bodySmall,
            color = if (fileError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FileRow(file: FeedbackFile, onRemove: (String) -> Unit) {
    val (icon, type) = when (file.kind) {
        FeedbackFile.Kind.PDF -> DesignR.drawable.ic_receipt_long to R.string.feedback_file_pdf
        FeedbackFile.Kind.IMAGE -> DesignR.drawable.ic_qr_code_scanner to R.string.feedback_file_image
        FeedbackFile.Kind.TEXT -> DesignR.drawable.ic_description to R.string.feedback_file_text
    }
    CoShellListRow(
        title = file.name,
        subtitle = stringResource(R.string.feedback_file_line, stringResource(type), (file.size / BYTES_IN_KB).coerceAtLeast(1)),
        leading = { IconBadge(icon, null) },
        trailing = {
            CoShellIconButton(onClick = { onRemove(file.path) }) {
                Icon(painterResource(DesignR.drawable.ic_close), contentDescription = stringResource(R.string.feedback_file_remove))
            }
        },
    )
}

private const val BYTES_IN_KB = 1024L
