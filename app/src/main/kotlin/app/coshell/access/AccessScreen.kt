package app.coshell.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellEmptyState
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.access.AccessCode
import app.coshell.core.designsystem.R as DesignR

/**
 * The code screen (#666, board `AccessGate`): shown while the phone has no valid code, in place of the whole app.
 * [onCancel] — only when the person came from «Доступ закрыт» and may go back to their data.
 */
@Composable
fun AccessScreen(entry: AccessEntry, onSubmit: (String) -> Unit, onCancel: (() -> Unit)? = null) {
    var typed by rememberSaveable { mutableStateOf("") }
    var where by rememberSaveable { mutableStateOf(false) }
    val ready = AccessCode.normalize(typed) != null
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(padding).padding(horizontal = Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            CoShellEmptyState(DesignR.drawable.ic_lock, stringResource(R.string.access_title))
            Text(
                stringResource(R.string.access_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            CoShellTextField(
                value = typed,
                onValueChange = { typed = AccessCode.format(it) },
                label = stringResource(R.string.access_field),
                modifier = Modifier.fillMaxWidth(),
                error = entry.error?.let { stringResource(it.text()) },
                enabled = !entry.busy,
                keyboardOptions = CodeKeyboard,
            )
            if (entry.busy) CoShellLinearProgress(Modifier.fillMaxWidth())
            CoShellButton(onClick = { onSubmit(typed) }, enabled = ready && !entry.busy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.access_continue))
            }
            CoShellTextButton(onClick = { where = true }) { Text(stringResource(R.string.access_where)) }
            if (onCancel != null) CoShellTextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
        }
    }
    if (where) {
        CoShellAlertDialog(
            title = stringResource(R.string.access_where_title),
            text = stringResource(R.string.access_where_text),
            confirmLabel = stringResource(R.string.access_where_ok),
            onConfirm = { where = false },
            onDismiss = { where = false },
        )
    }
}

private fun AccessError.text(): Int = when (this) {
    AccessError.MALFORMED -> R.string.access_err_malformed
    AccessError.NOT_FOUND -> R.string.access_err_not_found
    AccessError.REVOKED -> R.string.access_err_revoked
    AccessError.FULL -> R.string.access_err_full
    AccessError.NETWORK -> R.string.access_err_network
}
