package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.tokens.Spacing

/** Palette index of the wizard (CategoryPalette): orange — the «Ещё» tile and the «Выбрать файл» tile. */
object ImportColors {
    const val FILE = 12
    const val MENU = 12
}

/** Small heading over a group of rows inside a step. */
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.xs),
    )
}
