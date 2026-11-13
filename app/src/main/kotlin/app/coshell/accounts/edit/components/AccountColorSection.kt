package app.coshell.accounts.edit.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.BankColors
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.ColorStrip
import app.coshell.ui.form.SheetCaption

/**
 * «Цвет» of an account (#290, board `Fb290FAAcc`): one line of swatches with «Без цвета» first; when the name looks
 * like a bank, «Как у Galicia» on the right takes its colour. «+N» calls [onOpenAll] — the colour sheet with
 * [BankPresets] under the palette.
 */
@Composable
internal fun AccountColorSection(name: String, color: Long?, onSelect: (Long?) -> Unit, onOpenAll: () -> Unit) {
    val suggestion = BankColors.suggest(name)?.takeIf { it.color != color }
    CoShellFormSection(
        stringResource(R.string.category_color),
        trailing = suggestion?.let { bank -> { CoShellSectionLink(stringResource(R.string.account_bank_like, bank.name), { onSelect(bank.color) }) } },
    ) {
        ColorStrip(color, onSelect, onAll = onOpenAll, allowNone = true)
    }
}

/** Bank and payment-app colours as small badges with their initials, all groups in one block. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BankPresets(selected: Long?, onSelect: (Long?) -> Unit) {
    SheetCaption(stringResource(R.string.account_banks))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        BankColors.groups.flatMap { it.presets }.forEach { preset ->
            val chosen = preset.color == selected
            AccountBadge(
                preset.name,
                preset.color,
                size = Sizes.badgeSmall,
                modifier = Modifier
                    .semantics { contentDescription = preset.name }
                    .tactileClickable(Tactile.Control, CircleShape) { onSelect(preset.color) }
                    .selectionPop(chosen)
                    .border(Sizes.selectionRing, if (chosen) Color(preset.color) else Color.Transparent, CircleShape),
            )
        }
    }
}
