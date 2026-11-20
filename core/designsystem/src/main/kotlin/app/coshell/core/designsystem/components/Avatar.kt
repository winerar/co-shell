package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.tokens.Sizes

/**
 * A family member (board `CompNew`, «Аватар участника», #367 А): a circle of the member's [color] with the first
 * letter of [name] in ink by contrast. Default size — the small badge of a row.
 */
@Composable
fun CoShellAvatar(name: String, color: Color, modifier: Modifier = Modifier, size: Dp = Sizes.badgeSmall) {
    Box(modifier.size(size).background(color, CircleShape).fillEdge(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(1).uppercase(), color = inkOn(color), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}
