package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.ConfirmPulse
import app.coshell.core.designsystem.motion.MorphIcon
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.confirmProgress
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** One tab of [CoShellBottomBar]. */
@Immutable
data class CoShellTab(
    @DrawableRes val icon: Int,
    val label: String,
    val selected: Boolean,
    /** A small neon dot on the icon: something new inside (e.g. an app update under «Ещё»). */
    val badge: Boolean = false,
)

/**
 * Bottom bar (design-system.md §«Навигация и жесты», mockups in docs/design/): a floating capsule with
 * icon-only tabs and the round «+» in the middle — the most frequent action is always under the thumb.
 * Active tab: a wide neon pill of the palette's primary that flows from tab to tab (`TabPill.kt`), the
 * icon pops. Tab labels are read by TalkBack. [onTab] gets the tab's index in [tabs]; [addLabel] is the «+» for TalkBack.
 *
 * When [visible] turns false (an inner screen opened) the bar rides down off the screen and the space under
 * it closes with the same spring, so the screen above never jumps (design-refactor.md, Step 3).
 */
@Composable
fun CoShellBottomBar(
    tabs: List<CoShellTab>,
    onTab: (Int) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    visible: Boolean = true,
    addConfirm: ConfirmPulse? = null,
) {
    AnimatedVisibility(visible = visible, enter = BarIn, exit = BarOut) {
        BottomBarContent(tabs, onTab, addLabel, onAdd, addConfirm)
    }
}

/**
 * The «+»: after a save ([addConfirm] fires) it turns into a check mark with a short warm flash of the
 * palette's third colour (design-refactor Step 4: «Сохранено» — тёплая вспышка), then back.
 */
@Composable
private fun AddButton(label: String, onAdd: () -> Unit, addConfirm: ConfirmPulse?) {
    val scheme = MaterialTheme.colorScheme
    val progress = confirmProgress(addConfirm)
    val p = progress.value.coerceIn(0f, 1f)
    CoShellFab(
        onClick = onAdd,
        modifier = Modifier.size(Sizes.bottomBarAdd),
        shape = CircleShape,
        tactile = Tactile.Primary,
        containerColor = lerp(scheme.primary, scheme.tertiary, p),
        contentColor = lerp(scheme.onPrimary, scheme.onTertiary, p),
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        MorphIcon(
            progress = { progress.value },
            plus = { Icon(painterResource(R.drawable.ic_add), label) },
            check = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
        )
    }
}

private val BarIn = slideInVertically(Motion.Placement) { it } + expandVertically(Motion.Resize, expandFrom = Alignment.Top)
private val BarOut = slideOutVertically(Motion.Placement) { it } + shrinkVertically(Motion.Resize, shrinkTowards = Alignment.Top)

@Composable
private fun BottomBarContent(
    tabs: List<CoShellTab>,
    onTab: (Int) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    addConfirm: ConfirmPulse?,
) {
    val dark = LocalDarkTheme.current
    val scheme = MaterialTheme.colorScheme
    // One pill flows between tabs (TabPill.kt) instead of one per tab.
    val pillColor = if (dark) scheme.primary else scheme.primaryContainer
    val pill = rememberTabPillState()
    AnimateTabPill(pill, tabs.indexOfFirst { it.selected })
    // A floating capsule over the screen's background, clear of the system navigation bar.
    // Never fillMaxHeight here: the Scaffold measures the bar with the whole screen as its max height,
    // so the bar would take the screen and leave screens 0 px tall (releases #52/#56).
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Spacing.l),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Sizes.bottomBarHeight)
                .clip(PillShape)
                .background(scheme.surfaceContainerHigh)
                .tabPill(pill, pillColor)
                .padding(horizontal = Spacing.xs + Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                if (index == tabs.size / 2) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AddButton(addLabel, onAdd, addConfirm)
                    }
                }
                val iconColor = when {
                    !tab.selected -> scheme.onSurfaceVariant
                    dark -> scheme.onPrimary
                    else -> scheme.onPrimaryContainer
                }
                val tint by animateColorAsState(iconColor, Motion.effects(), label = "tabIcon")
                CoShellBarTab(
                    label = tab.label,
                    selected = tab.selected,
                    onClick = { onTab(index) },
                    pillModifier = Modifier.tabPillAnchor(pill, index),
                ) { TabIcon(tab, tint) }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: CoShellTab, tint: Color) {
    if (!tab.badge) return Icon(painterResource(tab.icon), contentDescription = null, tint = tint)
    BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.tertiary) }) {
        Icon(painterResource(tab.icon), contentDescription = null, tint = tint)
    }
}
