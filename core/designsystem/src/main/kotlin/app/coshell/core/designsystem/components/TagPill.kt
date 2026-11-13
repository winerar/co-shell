package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes

/**
 * A tag on an operation (#129, board `Fb129AOps`): a coloured dot and the name in a pill tinted with the tag's
 * [color] (a stored palette value — drawn by theme like category colours). Small — for list rows.
 */
@Composable
fun CoShellTagPill(name: String, color: Long, modifier: Modifier = Modifier) {
    val ink = categoryColor(color)
    Row(
        modifier
            .widthIn(max = TagPillMaxWidth)
            .height(TagPillHeight)
            .background(ink.copy(alpha = FillAlpha), PillShape)
            .border(Sizes.hairline, ink.copy(alpha = BorderAlpha), PillShape)
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).background(ink, CircleShape))
        Text(
            name,
            color = ink,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The tag's colour as a dot — leading mark of a tag row in lists and pickers. */
@Composable
fun CoShellTagDot(color: Long, modifier: Modifier = Modifier) {
    Box(modifier.size(TagDotSize).background(categoryColor(color), CircleShape))
}

/** «+2» after the shown pills: how many more tags the operation has. */
@Composable
fun CoShellTagMore(count: Int, modifier: Modifier = Modifier) {
    Text(
        "+$count",
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val TagPillHeight = 22.dp
private val TagDotSize = 14.dp

/** A long name («такси ночью из аэропорта») ends with «…» so two pills and «+N» still fit a row. */
private val TagPillMaxWidth = 132.dp
private const val FillAlpha = 0.18f
private const val BorderAlpha = 0.45f
