package app.coshell.transactions.list.bulk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellTagChip
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.bulk.BulkEditState.Step
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.categoryLines
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/**
 * The sheets behind «Изменить» (#794, boards `Bulk794*`): the menu with «Категория» and «Теги», then the one picked. Applying a
 * choice ends the selection mode ([onCategory], [onTags]); a swipe or × only closes ([onClose]).
 */
@Composable
internal fun BulkEditSheets(
    state: BulkEditState?,
    onStep: (Step) -> Unit,
    onCategory: (String) -> Unit,
    onTags: (List<String>) -> Unit,
    onClose: () -> Unit,
) {
    state ?: return
    when (state.step) {
        Step.MENU -> BulkEditMenu(state, onStep, onClose)
        Step.CATEGORY -> ChoiceSheet(
            title = stringResource(R.string.bulk_category),
            hint = stringResource(R.string.bulk_category_sheet_hint, state.count),
            options = categoryLines(state.categories, stringResource(R.string.category_kind_folder)),
            selected = null,
            onPick = onCategory,
            onDismiss = { onStep(Step.MENU) },
        )
        Step.TAGS -> BulkTagsSheet(state, onTags, onDismiss = { onStep(Step.MENU) })
    }
}

@Composable
private fun BulkEditMenu(state: BulkEditState, onStep: (Step) -> Unit, onClose: () -> Unit) {
    ModalSheet(
        title = stringResource(R.string.bulk_edit_title),
        subtitle = stringResource(R.string.bulk_edit_subtitle, state.count),
        onClosed = onClose,
    ) {
        CoShellRowGroup {
            row {
                if (state.categoryAllowed) {
                    CoShellMenuRow(
                        DesignR.drawable.ic_folder, stringResource(R.string.bulk_category), onClick = { onStep(Step.CATEGORY) },
                        subtitle = stringResource(R.string.bulk_category_hint),
                    )
                } else {
                    // Off: a dim row with the reason and no chevron; it takes no taps.
                    CoShellMenuRow(
                        DesignR.drawable.ic_folder, stringResource(R.string.bulk_category), onClick = {},
                        modifier = Modifier.alpha(DIM), subtitle = stringResource(R.string.bulk_category_off), trailing = {},
                    )
                }
            }
            row {
                CoShellMenuRow(
                    DesignR.drawable.ic_sell, stringResource(R.string.bulk_tags), onClick = { onStep(Step.TAGS) },
                    subtitle = stringResource(R.string.bulk_tags_hint),
                )
            }
        }
    }
}

/** «Теги»: chips to switch on, «Добавить к N» at the bottom; nothing is taken away from the rows. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BulkTagsSheet(state: BulkEditState, onTags: (List<String>) -> Unit, onDismiss: () -> Unit) {
    var chosen by remember { mutableStateOf(emptyList<String>()) }
    ModalSheet(
        title = stringResource(R.string.bulk_tags),
        subtitle = stringResource(R.string.bulk_tags_hint),
        onClosed = onDismiss,
        actionLabel = stringResource(R.string.bulk_tags_add, state.count),
        actionEnabled = chosen.isNotEmpty(),
        onAction = { onTags(chosen) },
    ) {
        if (state.tags.isEmpty()) {
            Text(stringResource(R.string.bulk_tags_empty))
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                state.tags.forEach { tag ->
                    CoShellTagChip(
                        tag.name, tag.color, selected = tag.id in chosen,
                        onClick = { chosen = if (tag.id in chosen) chosen - tag.id else chosen + tag.id },
                    )
                }
            }
        }
    }
}

private const val DIM = 0.38f
