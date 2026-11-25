package app.coshell.rates.list.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.tokens.Spacing

/** Section heading (К4) of the rates screen: «Курсы на сегодня», «Ручные курсы». */
@Composable
internal fun RatesSectionHeader(title: String) {
    CoShellSectionLabel(title, Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.s))
}
