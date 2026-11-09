package app.coshell.core.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii (owner's choice, design-questions §4): cards 16, sheets 28, fields 12,
 * buttons and chips fully round ("pills"). Material components pick them up through [CoShellShapes].
 */
object Radii {
    /** Small badges, swatches with corners. */
    val small = 8.dp
    /** Text fields, menus, snackbars. */
    val medium = 12.dp
    /** Cards and list groups. */
    val large = 16.dp
    /** Bottom sheets and dialogs. */
    val extraLarge = 28.dp
}

/** Fully round ends: buttons and chips. */
val PillShape = RoundedCornerShape(percent = 50)

/**
 * Material slots → our radii. extraSmall: text fields, menus, snackbar; small: chips (pill);
 * medium/large: cards; extraLarge: sheets, dialogs. Buttons are pills by Material default.
 */
internal val CoShellShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.medium),
    small = PillShape,
    medium = RoundedCornerShape(Radii.large),
    large = RoundedCornerShape(Radii.large),
    extraLarge = RoundedCornerShape(Radii.extraLarge),
)
