package app.coshell.core.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corner radii. Material components pick them up through [CoShellShapes]. */
object Radii {
    /** Chips, small badges. */
    val small = 8.dp
    /** Text fields, buttons. */
    val medium = 12.dp
    /** Cards and list groups. */
    val large = 16.dp
    /** Bottom sheets and dialogs. */
    val extraLarge = 28.dp
}

internal val CoShellShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(Radii.small),
    medium = RoundedCornerShape(Radii.medium),
    large = RoundedCornerShape(Radii.large),
    extraLarge = RoundedCornerShape(Radii.extraLarge),
)
