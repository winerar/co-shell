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
 * The card itself stays put.
 */
internal const val SHIFT = 8

/** [dir]: +1 going on (the loop from the last story to the first too), −1 going back — the screen knows which way it stepped. */
@Suppress("UnusedReceiverParameter")
internal fun AnimatedContentTransitionScope<Int>.storyTransition(dir: Int): ContentTransform {
    return (slideInHorizontally(Motion.Placement) { dir * it / SHIFT } + fadeIn(Motion.effects()))
        .togetherWith(slideOutHorizontally(Motion.Placement) { -dir * it / SHIFT } + fadeOut(Motion.effectsFast()))
        .using(SizeTransform(clip = false) { _, _ -> Motion.Resize })
}
