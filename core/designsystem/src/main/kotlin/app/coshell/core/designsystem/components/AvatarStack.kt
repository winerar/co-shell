package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Avatars overlapping (board `CompNew`, «Аватар участника», #367 А): [count] of them, each cuts a ring out of the one under
 * it, so the card's gradient shows through (an offscreen layer: [BlendMode.Clear] erases the avatars, not the card).
 * [avatar] draws the one at an index and must apply the given modifier.
 */
@Composable
fun CoShellAvatarStack(count: Int, modifier: Modifier = Modifier, avatar: @Composable (index: Int, modifier: Modifier) -> Unit) {
    Row(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        repeat(count) { index ->
            val shift = if (index == 0) {
                Modifier
            } else {
                Modifier.offset(x = -Spacing.m * index).drawBehind {
                    drawCircle(Color.Black, radius = size.minDimension / 2 + Sizes.avatarRing.toPx(), blendMode = BlendMode.Clear)
                }
            }
            avatar(index, shift)
        }
    }
}
