package app.coshell.accounts.edit.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.BankColors
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Bank and payment-app colours for an account: «Похоже на банк …» when the name matches a preset, then
 * the presets by country. The plain palette stays below in `ColorSection`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BankColorSection(name: String, selected: Long?, onSelect: (Long?) -> Unit) {
    val suggestion = BankColors.suggest(name)
    CoShellSection(stringResource(R.string.account_bank_colors)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellExpandable(suggestion != null && suggestion.color != selected) {
                if (suggestion == null) return@CoShellExpandable
                CoShellCard(onClick = { onSelect(suggestion.color) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    ) {
                        AccountBadge(suggestion.name, suggestion.color, size = Sizes.badgeSmall)
                        Text(
                            stringResource(R.string.account_bank_suggest, suggestion.name),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        CoShellTextButton(onClick = { onSelect(suggestion.color) }) {
                            Text(stringResource(R.string.account_bank_take))
                        }
                    }
                }
            }
            BankColors.groups.forEach { group ->
                Text(group.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    group.presets.forEach { preset ->
                        BankPreset(preset, preset.color == selected) { onSelect(preset.color) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankPreset(preset: BankColors.Preset, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(Radii.medium)
    Column(
        modifier = Modifier
            .width(Sizes.tileMinHeight / 2 + Spacing.l)
            .tactileClickable(Tactile.Control, shape, onClick = onClick)
            .selectionPop(selected)
            .border(Sizes.selectionRing, if (selected) Color(preset.color) else Color.Transparent, shape)
            .padding(vertical = Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AccountBadge(preset.name, preset.color)
        Text(
            preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
