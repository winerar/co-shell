package app.coshell.ui.hero

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The state of a service screen at a glance (mockups 11, 13, 14 — rates, family, backup): a hero card on the
 * theme's gradient, an icon in a circle filled with [tint], a title, a grey line under it and an optional [trailing] action.
 * [below] goes under the row inside the same card (a progress bar, a message).
 */
@Composable
fun StatusCard(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    below: (@Composable () -> Unit)? = null,
) {
    // A hero: the palette's gradient like «Общий баланс» (owner 2026-11-12, #277); [tint] colours the icon only.
    CoShellCard(modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.s), accent = true) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Box(Modifier.size(Sizes.badge).background(tint, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = null, tint = inkOn(tint), modifier = Modifier.size(Sizes.tileIcon))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                trailing?.invoke()
            }
            below?.invoke()
        }
    }
}
