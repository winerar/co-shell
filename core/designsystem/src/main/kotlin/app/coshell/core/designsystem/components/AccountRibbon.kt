package app.coshell.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.themeEdge
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** One account in [CoShellAccountRibbon]: the badge's name and colour (null — no colour). */
data class RibbonAccount(val id: String, val name: String, val color: Long?)

/**
 * One row of account badges (board `CompChoice` «Лента счетов», #505): the chosen account in a ring of the theme's
 * colour, «Все ›» at the end opens the full list. Cells are 48 dp to touch, the badge inside is [Sizes.ribbonBadge].
 */
@Composable
fun CoShellAccountRibbon(
    accounts: List<RibbonAccount>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAll: () -> Unit,
    allLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        accounts.forEach { account ->
            val chosen = account.id == selectedId
            val shape = RoundedCornerShape(Sizes.ribbonBadge * RING_RADIUS)
            Box(
                Modifier.size(Sizes.button)
                    .tactileClickable(Tactile.Control, shape, role = Role.RadioButton) { onSelect(account.id) }
                    .semantics { contentDescription = account.name; selected = chosen }
                    .selectionPop(chosen),
                contentAlignment = Alignment.Center,
            ) {
                val ring = if (chosen) Modifier.border(Sizes.selectionRing, themeEdge(), shape).padding(Spacing.xxs) else Modifier
                Box(Modifier.size(Sizes.ribbonBadge + Spacing.xs).then(ring), contentAlignment = Alignment.Center) {
                    AccountBadge(account.name, account.color, size = Sizes.ribbonBadge)
                }
            }
        }
        CoShellChoicePill(allLabel, selected = false, onClick = onAll)
    }
}

private const val RING_RADIUS = 0.4f
