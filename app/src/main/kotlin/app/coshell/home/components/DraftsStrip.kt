package app.coshell.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.R as DesignR

/** «Черновики · 5» under the balance (#586, board `Notes586Home`): shown only while drafts wait; a tap opens them. */
@Composable
internal fun DraftsStrip(count: Int, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        CoShellMenuRow(
            icon = DesignR.drawable.ic_draft,
            color = categoryColor(CategoryPalette.colors[STRIP_COLOR]),
            title = stringResource(R.string.drafts_title_count, count),
            subtitle = stringResource(R.string.drafts_strip_hint),
            onClick = onOpen,
        )
    }
}

private const val STRIP_COLOR = 13

@Preview(showBackground = true)
@Composable
private fun DraftsStripPreview() {
    CoShellTheme { DraftsStrip(5, {}) }
}
