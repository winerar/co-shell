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
import androidx.navigation.NavDestination
import app.coshell.core.designsystem.tokens.Motion

// Screen transitions (design-questions §6 and §9, issue #63). All on the spring tokens: the old curve
// finished 80 % of its way in the first 3–4 frames, so the movement was invisible (owner 2026-11-09).
// - Tabs of the bottom bar: a shared X axis — the new tab slides in from its side of the bar by an eighth of the
//   width, the old one leaves to the other side and fades.
// - A screen opened by a tap grows out of the tapped button or row while the old one steps back; going back
//   shrinks it into the same place (and follows the predictive back gesture). Without a tap — a shift by 1/8.
// The leaving screen fades on the slow effects spring: it is still visible while the new one moves in.
// Sheets (dialog destinations) move in `CoShellSheet`.

private const val SHIFT = 8
/** A new screen starts at this size around the tap. */
private const val GROW_FROM = 0.85f
/** The screen underneath steps back this far. */
private const val STEP_BACK = 0.94f

private typealias NavScope = AnimatedContentTransitionScope<NavBackStackEntry>

// The NavHost's four lambdas. Forward ones ask for the target's origin first: the tap is taken once, even for tabs.
internal fun NavScope.enter(origins: ScreenOrigins): EnterTransition {
    val origin = origins.forEnter(targetState.id)
    val dir = tabDirection()
    return if (dir != null) tabEnter(dir) else screenEnter(origin)
}

internal fun NavScope.exit(origins: ScreenOrigins): ExitTransition {
    val origin = origins.forEnter(targetState.id)
    val dir = tabDirection()
    return if (dir != null) tabExit(dir) else screenExit(newHasOrigin = origin != null)
}

internal fun NavScope.popEnter(origins: ScreenOrigins): EnterTransition {
    val dir = tabDirection()
    return if (dir != null) tabEnter(dir) else screenPopEnter(closingHasOrigin = origins.forPop(initialState.id) != null)
}

internal fun NavScope.popExit(origins: ScreenOrigins): ExitTransition {
    val dir = tabDirection()
    return if (dir != null) tabExit(dir) else screenPopExit(origins.forPop(initialState.id))
}

/** Both sides are tabs of the bottom bar → +1 (moving right along the bar), −1 (left); otherwise null. */
private fun NavScope.tabDirection(): Int? =
    tabDirection(tabIndex(initialState.destination), tabIndex(targetState.destination))

private fun tabIndex(destination: NavDestination): Int? =
    TopLevel.entries.indexOfFirst { destination.isOn(it) }.takeIf { it >= 0 }

/** Direction of a switch between bar tabs [from] → [to] (indices along the bar); null if either is not a tab. Pure. */
internal fun tabDirection(from: Int?, to: Int?): Int? {
    if (from == null || to == null) return null
    return if (to >= from) 1 else -1
}

private fun tabEnter(dir: Int): EnterTransition =
    slideInHorizontally(Motion.Placement) { dir * it / SHIFT } + fadeIn(Motion.effects())

private fun tabExit(dir: Int): ExitTransition =
    slideOutHorizontally(Motion.Placement) { -dir * it / SHIFT } + fadeOut(Motion.effectsSlow())

private fun screenEnter(origin: TransformOrigin?): EnterTransition =
    if (origin == null) slideInHorizontally(Motion.Placement) { it / SHIFT } + fadeIn(Motion.effects())
    else scaleIn(Motion.spatial(), initialScale = GROW_FROM, transformOrigin = origin) + fadeIn(Motion.effects())

private fun screenExit(newHasOrigin: Boolean): ExitTransition =
    if (!newHasOrigin) slideOutHorizontally(Motion.Placement) { -it / SHIFT } + fadeOut(Motion.effectsSlow())
    else scaleOut(Motion.spatial(), targetScale = STEP_BACK) + fadeOut(Motion.effectsSlow())

private fun screenPopEnter(closingHasOrigin: Boolean): EnterTransition =
    if (!closingHasOrigin) slideInHorizontally(Motion.Placement) { -it / SHIFT } + fadeIn(Motion.effects())
    else scaleIn(Motion.spatial(), initialScale = STEP_BACK) + fadeIn(Motion.effects())

private fun screenPopExit(origin: TransformOrigin?): ExitTransition =
    if (origin == null) slideOutHorizontally(Motion.Placement) { it / SHIFT } + fadeOut(Motion.effectsSlow())
    else scaleOut(Motion.spatial(), targetScale = GROW_FROM, transformOrigin = origin) + fadeOut(Motion.effectsSlow())
