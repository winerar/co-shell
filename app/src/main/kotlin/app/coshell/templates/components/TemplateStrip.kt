package app.coshell.templates.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.dashedBorder
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.templates.TemplateItem

/**
 * «Шаблоны · Все N ›» and a sideways strip of chips ending with a dashed «+» (mockup `TplA`). Without
 * templates — only the dashed «+ Новый шаблон», so the feature can be found.
 */
@Composable
fun TemplateStrip(
    items: List<TemplateItem>,
    total: Int,
    onPick: (String) -> Unit,
    onAll: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.templates_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (total > 0) {
                Text(
                    stringResource(R.string.templates_all, total),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentInk(),
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, PillShape, onClick = onAll)
                        .padding(horizontal = Spacing.s, vertical = Spacing.xs),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item -> TemplateChip(item, onClick = { onPick(item.id) }) }
            NewTemplateTile(withLabel = items.isEmpty(), onClick = onNew)
        }
    }
}

/** The dashed «+»: a square after the chips, a wide «+ Новый шаблон» when it is alone. */
@Composable
private fun NewTemplateTile(withLabel: Boolean, onClick: () -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    val label = stringResource(R.string.template_new)
    Row(
        modifier = Modifier
            .heightIn(min = Sizes.bottomBarAdd)
            .widthIn(min = Sizes.bottomBarAdd)
            .tactileClickable(Tactile.Control, MaterialTheme.shapes.large, onClick = onClick)
            .dashedBorder(line, Radii.large)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(DesignR.drawable.ic_add),
                contentDescription = if (withLabel) null else label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (withLabel) Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
