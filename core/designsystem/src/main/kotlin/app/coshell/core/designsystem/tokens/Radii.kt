package app.coshell.core.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii — four steps of «Единый стиль» (owner's poll 2026-11-15, #367, board Style4): 28 — what slides over
 * (sheet, dialog), 24 — everything card-like, 16 — text fields, capsule — everything pressed or chosen.
 * Material components pick them up through [CoShellShapes].
 */
object Radii {
    /** Small badges, swatches with corners. */
    val small = 8.dp
    /** Text fields. */
    val medium = 16.dp
    /** Cards, list groups, form cards, question cards, bubbles, menus, snackbars. */
    val large = 24.dp
    /** Bottom sheets and dialogs. */
    val extraLarge = 28.dp
}

/** Fully round ends: buttons and chips. */
val PillShape = RoundedCornerShape(percent = 50)

/**
 * Material slots → our radii. extraSmall: text fields; small: chips (pill); medium/large: cards, menus, snackbar;
 * extraLarge: sheets, dialogs. Buttons are pills by Material default.
 */
internal val CoShellShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.medium),
    small = PillShape,
    medium = RoundedCornerShape(Radii.large),
    large = RoundedCornerShape(Radii.large),
    extraLarge = RoundedCornerShape(Radii.extraLarge),
)
