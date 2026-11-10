package app.coshell.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Bottom bar tab as in the mockups: an icon without a label, the whole cell is the target. [pillModifier]
 * marks the slot the active-tab pill fills (`TabPill.kt`); the tab sinks under the finger, its icon pops
 * when it opens. [label] is read by TalkBack.
 */
@Composable
internal fun RowScope.CoShellBarTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    pillModifier: Modifier,
    icon: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        Modifier
            .weight(1f)
            .height(Sizes.bottomBarHeight)
            .tactile(source, Tactile.Control, PillShape)
            .selectable(selected, interactionSource = source, indication = null, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxs)
                .then(pillModifier)
                .height(Sizes.bottomBarPill),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.selectionPop(selected)) { icon() }
        }
    }
}
