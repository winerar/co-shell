package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/** Which header: of a tab, of an inner screen, or of a list in selection mode (board `CompNew`, «Шапка экрана», #367). */
enum class TopBarKind { Tab, Screen, Selection }

/**
 * Screen header (board `CompNew`, «Шапка экрана», #367 А): a 64 dp row — [navigation] («Назад», «✕»), the title, [actions].
 * The bar hands its [kind]'s text style to [title] (a tab's is bigger), so screens never set fonts themselves.
 * Tab and Screen are the colour of the screen; once [scrolled] a hairline fades in under them. Selection is a step
 * lighter, always solid. Reaches under the status bar by itself.
 */
@Composable
fun CoShellTopBar(
    kind: TopBarKind,
    modifier: Modifier = Modifier,
    scrolled: Boolean = false,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable (TextStyle) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val fill = if (kind == TopBarKind.Selection) scheme.surfaceContainerHigh else scheme.surface
    val base = if (kind == TopBarKind.Tab) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    Row(
        modifier
            .fillMaxWidth()
            .hairlineBelow(scrolled && kind != TopBarKind.Selection)
            .background(fill)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .height(BAR_HEIGHT)
            .padding(horizontal = EDGE),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigation != null) {
            navigation()
            Box(Modifier.width(NAV_GAP))
        } else {
            Box(Modifier.width(TITLE_INSET))
        }
        Box(Modifier.weight(1f)) { title(base.copy(fontWeight = FontWeight.Bold)) }
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** Thin line along the bottom edge that fades in with [visible]; drawn over the bar, so it never moves the content. */
@Composable
private fun Modifier.hairlineBelow(visible: Boolean): Modifier {
    val color = MaterialTheme.colorScheme.outlineVariant
    val alpha by animateFloatAsState(if (visible) 1f else 0f, Motion.effects(), label = "hairline")
    return drawWithContent {
        drawContent()
        val width = Sizes.hairline.toPx()
        val y = size.height - width / 2
        drawLine(color.copy(alpha = alpha), Offset(0f, y), Offset(size.width, y), width)
    }
}

private val BAR_HEIGHT = 64.dp
private val EDGE = 4.dp
private val TITLE_INSET = 12.dp
private val NAV_GAP = 4.dp
