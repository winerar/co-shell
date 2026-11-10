package app.coshell.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The capsule of [CoShellBottomBar] with actions instead of tabs — e.g. «Дублировать · Удалить · 3» of a list's
 * selection mode (mockup 19В, issue #112). Same place, size and spring as the bar it replaces, so the swap
 * reads as one bar changing its buttons. Give the buttons `Modifier.weight(1f)` to split the capsule.
 */
@Composable
fun CoShellActionBar(visible: Boolean, content: @Composable RowScope.() -> Unit) {
    AnimatedVisibility(visible = visible, enter = BarIn, exit = BarOut) {
        Box(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Spacing.l),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(Sizes.bottomBarHeight)
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                content = content,
            )
        }
    }
}
