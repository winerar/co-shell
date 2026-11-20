package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A tag on an operation (#129): a label (board Style2) — a 22 dp capsule tinted 18 % with the tag's [color] (a stored
 * palette value — drawn by theme like category colours), the colour also in the dot, the name in ink (#385).
 */
@Composable
fun CoShellTagPill(name: String, color: Long, modifier: Modifier = Modifier) {
    val ink = categoryColor(color)
    Row(
        modifier
            .widthIn(max = TagPillMaxWidth)
            .labelCapsule(ink),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).background(ink, CircleShape).fillEdge(ink, CircleShape))
        Text(
            name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The tag's colour as a dot — leading mark of a tag row in lists and pickers. */
@Composable
fun CoShellTagDot(color: Long, modifier: Modifier = Modifier) {
    val fill = categoryColor(color)
    Box(modifier.size(TagDotSize).background(fill, CircleShape).fillEdge(fill, CircleShape))
}

/** «+2» after the shown pills: how many more tags the operation has. */
@Composable
fun CoShellTagMore(count: Int, modifier: Modifier = Modifier) {
    Text(
        "+$count",
        modifier = modifier,
        maxLines = 1,
        softWrap = false,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val TagDotSize = 14.dp

/** A long name («такси ночью из аэропорта») ends with «…» so two pills and «+N» still fit a row. */
private val TagPillMaxWidth = 132.dp

/**
 * The one label shape (board Style2: tag, change, status, «новое»): [Sizes.labelHeight] capsule, 8 dp sides, the
 * [color] at 18 % behind ink text; the colour itself goes only into a dot or an arrow.
 */
internal fun Modifier.labelCapsule(color: Color): Modifier =
    heightIn(min = Sizes.labelHeight).background(color.copy(alpha = LABEL_TINT), PillShape).padding(horizontal = Spacing.s)

internal const val LABEL_TINT = 0.18f
