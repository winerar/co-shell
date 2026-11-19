package app.coshell.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes

/**
 * Four rounded corners of a viewfinder over the camera (scanner of a receipt QR, #526, board «QrScan»):
 * a square of [Sizes.scanFrame] in the neon of the theme. Draws only; the camera lies under it.
 */
@Composable
fun CoShellScanFrame(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.size(Sizes.scanFrame)) {
        val stroke = Stroke(width = Sizes.scanFrameStroke.toPx(), cap = StrokeCap.Round)
        val radius = Sizes.scanFrameCorner.toPx()
        val arm = Sizes.scanFrameArm.toPx()
        val half = stroke.width / 2
        val w = size.width - half
        val h = size.height - half
        fun corner(x: Float, y: Float, dx: Float, dy: Float) = Path().apply {
            moveTo(x, y + dy * (arm + radius))
            lineTo(x, y + dy * radius)
            quadraticTo(x, y, x + dx * radius, y)
            lineTo(x + dx * (arm + radius), y)
        }
        drawPath(corner(half, half, 1f, 1f), color, style = stroke)
        drawPath(corner(w, half, -1f, 1f), color, style = stroke)
        drawPath(corner(w, h, -1f, -1f), color, style = stroke)
        drawPath(corner(half, h, 1f, -1f), color, style = stroke)
    }
}

@Preview(widthDp = 320, heightDp = 320)
@Composable
private fun ScanFramePreview() {
    CoShellTheme { CoShellScanFrame() }
}
