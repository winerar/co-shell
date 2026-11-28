package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.BankColors
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CoShellBankDot
import app.coshell.ui.form.SheetCaption

/**
 * «Как у Galicia» (#290): when the name looks like a bank, a link that takes its colour. Since #818 (poll «А») the colour is
 * picked from the badge beside the name — the link sits at the top of that sheet, over [BankPresets].
 */
@Composable
internal fun BankSuggestion(name: String, color: Long?, onSelect: (Long?) -> Unit) {
    val bank = BankColors.suggest(name)?.takeIf { it.color != color } ?: return
    CoShellSectionLink(stringResource(R.string.account_bank_like, bank.name), { onSelect(bank.color) })
}

/** Bank and payment-app colours as small badges with their initials, all groups in one block. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BankPresets(selected: Long?, onSelect: (Long?) -> Unit) {
    SheetCaption(stringResource(R.string.account_banks))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        BankColors.groups.flatMap { it.presets }.forEach { preset ->
            CoShellBankDot(preset.name, preset.color, preset.color == selected) { onSelect(preset.color) }
        }
    }
}
