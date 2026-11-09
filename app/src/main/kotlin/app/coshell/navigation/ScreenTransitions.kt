package app.coshell.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import app.coshell.core.designsystem.tokens.Motion

// Screen transitions (design-questions §6): a short shift by a tenth of the width plus a fade —
// noticeable but quick (≤ 350 ms). Going back mirrors it and follows the predictive back gesture.
// Sheets (dialog destinations) have their own slide-up.

private const val SHIFT = 10

internal fun screenEnter(): EnterTransition =
    slideInHorizontally(Motion.enter()) { it / SHIFT } + fadeIn(Motion.enter())

internal fun screenExit(): ExitTransition =
    slideOutHorizontally(Motion.enter()) { -it / SHIFT } + fadeOut(Motion.exit())

internal fun screenPopEnter(): EnterTransition =
    slideInHorizontally(Motion.enter()) { -it / SHIFT } + fadeIn(Motion.enter())

internal fun screenPopExit(): ExitTransition =
    slideOutHorizontally(Motion.enter()) { it / SHIFT } + fadeOut(Motion.exit())
