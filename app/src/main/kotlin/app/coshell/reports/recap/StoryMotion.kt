package app.coshell.reports.recap

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import app.coshell.core.designsystem.tokens.Motion

/**
 * Motion between the stories of [MonthRecapScreen] (#554): the next story slides in by 1/[SHIFT] of the width with the same spring
 * as tabs and screens (`navigation/ScreenTransitions`), the old one leaves the other way; fade on the effects springs.
 * The direction is [storyDirection]; the card itself stays put.
 */
internal const val SHIFT = 8

/** +1 going on, −1 going back; the jump from the last story to the first is «on» (the tap loops forward). Pure. */
internal fun storyDirection(from: Int, to: Int, last: Int): Int = when {
    from == last && to == 0 && last > 0 -> 1
    to >= from -> 1
    else -> -1
}

internal fun AnimatedContentTransitionScope<Int>.storyTransition(last: Int): ContentTransform {
    val dir = storyDirection(initialState, targetState, last)
    return (slideInHorizontally(Motion.Placement) { dir * it / SHIFT } + fadeIn(Motion.effects()))
        .togetherWith(slideOutHorizontally(Motion.Placement) { -dir * it / SHIFT } + fadeOut(Motion.effectsFast()))
        .using(SizeTransform(clip = false) { _, _ -> Motion.Resize })
}
