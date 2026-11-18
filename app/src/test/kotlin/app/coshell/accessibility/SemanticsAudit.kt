package app.coshell.accessibility

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull

/**
 * Accessibility audit over the Compose semantics tree (#380). Google's ATF cannot run under Robolectric: every one of
 * its checks comes back `NOT_RUN` (checked 2026-11-16, ATF 4.1.1), so the tree is walked here instead. Two rules:
 * a tap target is at least [MIN_TARGET_DP] on both sides, and every tappable thing has a spoken label.
 * Contrast is covered by `ColorContrastTest` / `NonTextContrastTest` (from the tokens, not pixels).
 */
object SemanticsAudit {
    const val MIN_TARGET_DP = 48f

    /** One line per violation: `rule | label | size`; the lines are what the exception list stores. */
    fun audit(root: SemanticsNode, density: Float): List<String> {
        val found = mutableListOf<String>()
        walk(root) { node ->
            if (!node.isTappable()) return@walk
            val label = node.label()
            val bounds = node.touchBoundsInRoot
            if (bounds.isEmpty) return@walk // scrolled out of the viewport: nothing to tap
            val w = bounds.width / density
            val h = bounds.height / density
            if (label.isEmpty()) found += "no-label | ${node.role()} | ${w.fmt()}x${h.fmt()}"
            if (w < MIN_TARGET_DP - EPS || h < MIN_TARGET_DP - EPS) {
                found += "small-target | ${label.ifEmpty { node.role() }.take(40)} | ${w.fmt()}x${h.fmt()}"
            }
        }
        return found
    }

    private fun walk(node: SemanticsNode, visit: (SemanticsNode) -> Unit) {
        visit(node)
        node.children.forEach { walk(it, visit) }
    }

    private fun SemanticsNode.isTappable(): Boolean {
        val c = config
        if (c.contains(SemanticsProperties.Disabled)) return false
        return c.contains(SemanticsActions.OnClick) || c.contains(SemanticsProperties.ToggleableState) ||
            c.contains(SemanticsProperties.Selected)
    }

    private fun SemanticsNode.label(): String {
        val c = config
        val text = c.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }.orEmpty()
        val desc = c.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ").orEmpty()
        return "$desc $text".trim()
    }

    private fun SemanticsNode.role(): String = config.getOrNull(SemanticsProperties.Role)?.toString() ?: "node"

    private fun Float.fmt(): String = if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

    private const val EPS = 0.5f
}
