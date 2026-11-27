package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.RowScope
import app.coshell.core.designsystem.components.coShellButtonColors
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Buttons of the selection bar (mockup 19В, #794 boards `Bulk794*`): «Дублировать» — an icon, only for one chosen row (a copy
 * opens in the editor), «Изменить» — category or tags for all chosen rows, «Удалить · N» — at once, with «Отменить» in the snackbar.
 */
@Composable
internal fun RowScope.SelectionActions(count: Int, onDuplicate: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    CoShellIconButton(onClick = onDuplicate, enabled = count == 1) {
        Icon(
            painterResource(DesignR.drawable.ic_content_copy),
            contentDescription = stringResource(R.string.action_duplicate),
            modifier = Modifier.size(Sizes.buttonIcon),
        )
    }
    CoShellOutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(Sizes.actionBarButton)) {
        Icon(painterResource(DesignR.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(Sizes.buttonIcon))
        Spacer(Modifier.width(Spacing.s))
        Text(stringResource(R.string.action_edit_selected))
    }
    CoShellButton(
        onClick = onDelete,
        modifier = Modifier.weight(1f).height(Sizes.actionBarButton),
        colors = coShellButtonColors(colors.errorContainer, colors.onErrorContainer),
    ) {
        Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(Sizes.buttonIcon))
        Spacer(Modifier.width(Spacing.s))
        Text(stringResource(R.string.transactions_delete_selected, count))
    }
}
