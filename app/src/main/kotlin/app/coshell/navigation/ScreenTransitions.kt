package app.coshell.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import app.coshell.core.designsystem.tokens.Motion

// Screen transitions (design-questions §6, issue #63). A screen opened by a tap grows out of the tapped
// button or row while the old one steps back; going back shrinks it into the same place (and follows the
// predictive back gesture). Without a tap (shake, restored state) — a short shift by a tenth of the width.
// Tabs of the bottom bar are siblings: they only fade through. Scale and alpha only (performance.md), ≤ 350 ms.
// Sheets (dialog destinations) grow in `CoShellSheet`.

private const val SHIFT = 10
/** A new screen starts at this size around the tap. */
private const val GROW_FROM = 0.85f
/** The screen underneath steps back this far. */
private const val STEP_BACK = 0.94f
/** Tabs come in from just below full size. */
private const val TAB_FROM = 0.97f

private typealias NavScope = AnimatedContentTransitionScope<NavBackStackEntry>

// The NavHost's four lambdas. Forward ones ask for the target's origin first: the tap is taken once, even for tabs.
internal fun NavScope.enter(origins: ScreenOrigins): EnterTransition {
    val origin = origins.forEnter(targetState.id)
    return if (isTabSwitch()) tabEnter() else screenEnter(origin)
}

internal fun NavScope.exit(origins: ScreenOrigins): ExitTransition {
    val origin = origins.forEnter(targetState.id)
    return if (isTabSwitch()) tabExit() else screenExit(newHasOrigin = origin != null)
}

internal fun NavScope.popEnter(origins: ScreenOrigins): EnterTransition =
    if (isTabSwitch()) tabEnter() else screenPopEnter(closingHasOrigin = origins.forPop(initialState.id) != null)

internal fun NavScope.popExit(origins: ScreenOrigins): ExitTransition =
    if (isTabSwitch()) tabExit() else screenPopExit(origins.forPop(initialState.id))

/** Both sides are tabs of the bottom bar (siblings, not a screen opened from another). */
private fun NavScope.isTabSwitch(): Boolean =
    TopLevel.entries.any { initialState.destination.isOn(it) } && TopLevel.entries.any { targetState.destination.isOn(it) }

private fun screenEnter(origin: TransformOrigin?): EnterTransition =
    if (origin == null) slideInHorizontally(Motion.enter()) { it / SHIFT } + fadeIn(Motion.enter())
    else scaleIn(Motion.enter(), initialScale = GROW_FROM, transformOrigin = origin) + fadeIn(Motion.enter())

private fun screenExit(newHasOrigin: Boolean): ExitTransition =
    if (!newHasOrigin) slideOutHorizontally(Motion.enter()) { -it / SHIFT } + fadeOut(Motion.exit())
    else scaleOut(Motion.enter(), targetScale = STEP_BACK) + fadeOut(Motion.enter())

private fun screenPopEnter(closingHasOrigin: Boolean): EnterTransition =
    if (!closingHasOrigin) slideInHorizontally(Motion.enter()) { -it / SHIFT } + fadeIn(Motion.enter())
    else scaleIn(Motion.enter(), initialScale = STEP_BACK) + fadeIn(Motion.enter())

private fun screenPopExit(origin: TransformOrigin?): ExitTransition =
    if (origin == null) slideOutHorizontally(Motion.enter()) { it / SHIFT } + fadeOut(Motion.exit())
    else scaleOut(Motion.enter(), targetScale = GROW_FROM, transformOrigin = origin) + fadeOut(Motion.enter())

private fun tabEnter(): EnterTransition = fadeIn(Motion.medium()) + scaleIn(Motion.medium(), initialScale = TAB_FROM)

private fun tabExit(): ExitTransition = fadeOut(Motion.exit())
