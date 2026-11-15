package app.coshell.recurring.list.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.recurring.list.PausedUi

/**
 * «Счёт «Наличные» в архиве — платёж «Аренда» на паузе. Выберите другой счёт.» (#406, board `Rec143Edge`).
 * A tap opens the template, where the account is chosen.
 */
@Composable
internal fun PausedMessage(paused: PausedUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = paused.accountName?.let { stringResource(R.string.recurring_paused, it, paused.name) }
        ?: stringResource(R.string.recurring_paused_gone, paused.name)
    CoShellMessage(
        text,
        isError = true,
        modifier = modifier.tactileClickable(Tactile.Control, MaterialTheme.shapes.large, role = Role.Button, onClick = onClick),
    )
}
