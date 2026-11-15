package app.coshell.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.MemberRow

/**
 * «Кто сколько потратил» (#189, board `Rp189WhoA`): a bar per family member by the operations they added, the folders
 * they spent most on under it. «Я» — everything the others did not add. A tap opens the member's operations (#373).
 */
@Composable
internal fun MembersCard(members: List<MemberRow>, modifier: Modifier = Modifier, onMember: ((String?) -> Unit)? = null) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reports_members_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text(stringResource(R.string.reports_members_caption), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val me = stringResource(R.string.reports_members_me)
            val deleted = stringResource(R.string.reports_deleted_category)
            Column(Modifier.padding(top = Spacing.xs)) {
                members.forEach { row ->
                    val name = row.name ?: me
                    val color = categoryColor(CategoryPalette.forKey(row.memberId ?: ME_KEY))
                    CoShellBarRow(
                        label = name,
                        value = row.value,
                        share = "${row.percent}%",
                        fraction = row.fraction,
                        color = color,
                        leading = { Initial(name, color) },
                        caption = row.top.joinToString(" · ") { "${it.name ?: deleted} ${it.value}" },
                        onClick = onMember?.let { open -> { open(row.memberId) } },
                    )
                }
            }
            Text(
                stringResource(R.string.reports_members_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@Composable
private fun Initial(name: String, color: Color) {
    Box(Modifier.size(Sizes.badgeSmall).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(1).uppercase(), color = inkOn(color), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** The colour key of «Я»: the others are keyed by their id. */
private const val ME_KEY = "me"
