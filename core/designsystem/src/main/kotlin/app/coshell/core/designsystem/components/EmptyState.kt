package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * An empty screen (boards `HomeEmpty`, `AccountsEmpty`, catalog brick `empty`): an icon tile, one sentence that says what
 * to do, and — when there is something to press — the main button with a «+». Centered, the first thing the person sees.
 */
@Composable
fun CoShellEmptyState(
    @DrawableRes iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.fillMaxWidth().padding(top = Spacing.xxl * 3, start = Spacing.xl, end = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.l + Spacing.xxs),
    ) {
        Box(
            Modifier.size(Sizes.emptyTile).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(Spacing.xl)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = accentInk(), modifier = Modifier.size(Sizes.emptyIcon))
        }
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            CoShellButton(onClick = onAction) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(Sizes.buttonIcon))
                Text(actionLabel, modifier = Modifier.padding(start = Spacing.xs))
            }
        }
    }
}
