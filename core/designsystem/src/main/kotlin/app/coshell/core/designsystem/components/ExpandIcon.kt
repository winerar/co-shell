package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.tokens.Motion

/**
 * The "open / close" arrow of a drop-down (a folder of categories): points down when closed and turns
 * over with a spring when [expanded]. Turning happens in the graphics layer — no recomposition per frame.
 */
@Composable
fun CoShellExpandIcon(expanded: Boolean, contentDescription: String?, modifier: Modifier = Modifier) {
    val turn by animateFloatAsState(if (expanded) HALF_TURN else 0f, Motion.Turn, label = "expandArrow")
    Icon(
        painterResource(R.drawable.ic_keyboard_arrow_down),
        contentDescription = contentDescription,
        modifier = modifier.graphicsLayer { rotationZ = turn },
    )
}

private const val HALF_TURN = 180f
