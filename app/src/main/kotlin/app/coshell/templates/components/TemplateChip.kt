package app.coshell.templates.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem

/** A template as a chip: category badge, name, amount or «сумма при вводе» (mockups `TplA`, `TplAll` «Часто»). */
@Composable
fun TemplateChip(item: TemplateItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .tactileClickable(Tactile.Control, shape, onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .border(Sizes.hairline, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(start = Spacing.xs + Spacing.xxs, end = Spacing.m, top = Spacing.xs + Spacing.xxs, bottom = Spacing.xs + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        TemplateBadge(item)
        Column(Modifier.widthIn(max = Sizes.tileMinHeight)) {
            Text(
                item.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.amount.ifEmpty { stringResource(R.string.template_amount_on_entry) },
                style = RateTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The category's badge; a transfer (or a template without a category) gets the neutral one. */
@Composable
fun TemplateBadge(item: TemplateItem, modifier: Modifier = Modifier) {
    when {
        item.type == TransactionType.TRANSFER -> IconBadge(DesignR.drawable.ic_sync_alt, color = null, modifier = modifier)
        item.categoryName.isEmpty() -> IconBadge(DesignR.drawable.ic_bolt, color = null, modifier = modifier)
        else -> CategoryBadge(item.categoryIcon, item.categoryColor, modifier)
    }
}
