package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Drop-down menu (board `CompNew`, «Выпадающее меню», #367 А): a 22 dp card with [CoShellMenuItem] capsules inside.
 * Screens use this, not the Material one.
 */
@Composable
fun CoShellDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(MENU_RADIUS),
        shadowElevation = 0.dp, // no shadows (#477): the edge is a hairline
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        content = content,
    )
}

/**
 * One line of the menu: a 44 dp capsule. [checked] — the chosen one: tone, bold and «✓» on the right; [destructive] —
 * red; a disabled one is at 38 %.
 */
@Composable
fun CoShellMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    destructive: Boolean = false,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val ink = if (destructive) scheme.error else scheme.onSurface
    Row(
        modifier
            .padding(horizontal = Spacing.xs)
            .fillMaxWidth()
            .height(ITEM_HEIGHT)
            .alpha(if (enabled) 1f else DISABLED)
            .tactileClickable(Tactile.Row, CircleShape, enabled = enabled, role = Role.Button, onClick = onClick)
            .background(if (checked) scheme.primaryContainer else Color.Transparent, CircleShape)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (checked) FontWeight.Bold else null,
            color = ink,
            modifier = Modifier.weight(1f),
        )
        if (checked) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
    }
}

private val MENU_RADIUS = 22.dp
private val ITEM_HEIGHT = 44.dp
private const val DISABLED = 0.38f
