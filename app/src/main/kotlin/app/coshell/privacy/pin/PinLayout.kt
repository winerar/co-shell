package app.coshell.privacy.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.keypad.CoShellPinDots
import app.coshell.core.designsystem.components.keypad.CoShellPinPad
import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.core.designsystem.motion.errorShake
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.privacy.PinEntry
import app.coshell.core.designsystem.R as DesignR

/** What a PIN screen shows: the lock screen, creating, repeating or confirming the current PIN. */
data class PinUi(
    val title: String,
    val message: String = "",
    val error: Boolean = false,
    /** Changes on every new error, so the dots shake again for a second wrong PIN. */
    val errorKey: Int = 0,
    val filled: Int = 0,
    /** Keys answer (false while waiting after many wrong attempts or while the PIN is being checked). */
    val enabled: Boolean = true,
)

/**
 * The one layout of every PIN screen (#145, boards `Priv145LockA`, `Priv145Set1`, `Priv145Set2`): the lock in a neon circle,
 * the title, a line under it, the dots, and the pad at the bottom under the thumb; [link] — «Забыли PIN?» under the pad.
 * The caller's [modifier] carries the system-bar insets (the `Scaffold` padding).
 */
@Composable
fun PinLayout(
    ui: PinUi,
    onKey: (PinKey) -> Unit,
    modifier: Modifier = Modifier,
    biometricLabel: String? = null,
    link: Pair<String, () -> Unit>? = null,
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = Spacing.xl).padding(bottom = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        IconBadge(DesignR.drawable.ic_lock, MaterialTheme.colorScheme.primary, size = Sizes.badgeLarge)
        Text(
            ui.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.l),
        )
        Text(
            ui.message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (ui.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.s).heightIn(min = Sizes.badgeSmall),
        )
        CoShellPinDots(
            filled = ui.filled,
            description = stringResource(R.string.lock_dots, ui.filled, PinEntry.LENGTH),
            error = ui.error,
            modifier = Modifier.padding(top = Spacing.l).errorShake(ui.errorKey.takeIf { ui.error }),
        )
        Spacer(Modifier.weight(1f))
        CoShellPinPad(
            onKey = onKey,
            backspaceLabel = stringResource(R.string.keypad_backspace),
            biometricLabel = biometricLabel,
            enabled = ui.enabled,
        )
        Box(Modifier.fillMaxWidth().heightIn(min = Sizes.button).padding(top = Spacing.s), contentAlignment = Alignment.Center) {
            link?.let { (text, action) -> CoShellTextButton(onClick = action) { Text(text) } }
        }
    }
}

