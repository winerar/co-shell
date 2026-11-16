package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One capsule per item ([total] of them, board `CompData`, «Строка-столбик», #367 А; a bar of [Sizes.bullet] like every
 * bar): the first [kept] in [keptColor], the rest in [restColor] — «Уложились 4 из 6».
 */
@Composable
fun CoShellSegmentBar(total: Int, kept: Int, keptColor: Color, restColor: Color, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        repeat(total) { index ->
            Box(Modifier.weight(1f).height(Sizes.bullet).background(if (index < kept) keptColor else restColor, PillShape))
        }
    }
}
