package app.coshell.family.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.components.dashedBorder
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.model.Member
import app.coshell.core.designsystem.R as DesignR

/** A member's round avatar: the first letter of the name on the member's colour (join order, board `Family`). */
@Composable
internal fun MemberAvatar(member: Member, index: Int, modifier: Modifier = Modifier, size: Dp = Sizes.badge) {
    val fill = categoryColor(MemberColors[index % MemberColors.size])
    Box(modifier.size(size).background(fill, CircleShape), contentAlignment = Alignment.Center) {
        Text(
            memberInitial(member),
            color = inkOn(fill),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** The empty slot of «Пригласить»: a dashed ring with «+». */
@Composable
internal fun InviteAvatar() {
    Box(
        Modifier.size(Sizes.badge).dashedBorder(MaterialTheme.colorScheme.outline, Sizes.badge),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(DesignR.drawable.ic_add),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.tileIcon),
        )
    }
}

/** «Михаил» → «М», no name → the e-mail's first letter, nothing → «·». */
internal fun memberInitial(member: Member): String =
    (member.name.ifBlank { member.email }).firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "·"

/** Avatar colours of members, in join order: violet, cyan, then warm ones (mockup 13). */
private val MemberColors: List<Long> = listOf(3, 6, 5, 2).map { CategoryPalette.colors[it] }
