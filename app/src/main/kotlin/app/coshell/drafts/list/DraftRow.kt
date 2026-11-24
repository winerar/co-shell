package app.coshell.drafts.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.ui.ai.BetaMark
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.drafts.DraftMissing
import app.coshell.core.model.DraftCategorySource
import app.coshell.ui.format.formatDayMonthShort
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * A draft (#586, board `Notes586Drafts`): tick (any row: ready ones to post, any to delete, #628), title, «счёт · день», then the category with where
 * it came from or the mark of what is missing; the amount on the right. A tap on the row opens the entry sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DraftRow(row: DraftRowUi, today: LocalDate, onClick: () -> Unit, onToggle: () -> Unit) {
    CoShellListRow(
        // The amount shares the title's line, so the captions under it keep the whole width («Выберите категорию» is not cut).
        title = {
            // A long title keeps its words and the amount drops under it (wide amount, big font): not «К…» beside «−3 500,00 ARS».
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(row.title.ifBlank { stringResource(R.string.drafts_untitled) }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (row.amount != null) {
                        AmountText(row.amount, style = AmountTextStyle, minFontSize = MinAmountSize)
                    } else {
                        Text("—", style = AmountTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        supporting = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    // As in an operation row (#505): the account's mini badge before its name; no account — no badge.
                    if (row.accountName != null) AccountBadge(row.accountName, row.accountColor, size = Sizes.miniBadge, initials = false)
                    Text(
                        rowSub(row, today),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Missing(row)
            }
        },
        leading = {
            Box(
                Modifier.sizeIn(minWidth = Sizes.button, minHeight = Sizes.button)
                    .tactileClickable(role = Role.Checkbox, onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) { CoShellCheckMark(if (row.checked) ToggleableState.On else ToggleableState.Off) }
        },
        onClick = onClick,
    )
}

@Composable
private fun rowSub(row: DraftRowUi, today: LocalDate): String {
    val day = when (row.date) {
        today -> stringResource(R.string.drafts_day_today)
        today.minus(1, DateTimeUnit.DAY) -> stringResource(R.string.drafts_day_yesterday)
        else -> formatDayMonthShort(row.date)
    }
    return row.accountName?.let { stringResource(R.string.drafts_row_sub, it, day) } ?: day
}

/** What is missing («Нужна сумма»), else the grey «Кафе · по прошлым операциям». */
@Composable
private fun Missing(row: DraftRowUi) {
    val text = when (row.missing) {
        DraftMissing.AMOUNT -> R.string.drafts_missing_amount
        DraftMissing.ACCOUNT -> R.string.drafts_missing_account
        DraftMissing.CATEGORY -> R.string.drafts_missing_category
        null -> null
    }
    if (text != null) {
        Row { CoShellStatusChip(stringResource(text), StatusTone.Work) }
        return
    }
    val category = row.categoryName ?: return
    val source = when (row.source) {
        DraftCategorySource.HISTORY -> stringResource(R.string.entry_category_guess_history)
        DraftCategorySource.DICTIONARY -> stringResource(R.string.entry_category_guess_name)
        DraftCategorySource.AI -> stringResource(R.string.drafts_category_ai)
        null -> null
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        CategoryBadge(row.categoryIcon, row.categoryColor, size = Sizes.categoryMini)
        Text(
            if (source != null) stringResource(R.string.drafts_caption, category, source) else category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (row.source == DraftCategorySource.AI) BetaMark()
    }
}

private val MinAmountSize = 9.sp
