package app.coshell.converter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.converter.ConverterActions
import app.coshell.converter.ConverterUiState
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateConvertedStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «Все валюты» (board `Conv2AllType`): every currency of the list with the amount in it; the source row is lit and holds the typed
 * number, a tap on another row counts from it.
 */
@Composable
internal fun ConverterAll(state: ConverterUiState, actions: ConverterActions, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        state.rows.forEach { row ->
            CoShellListRow(
                title = row.code,
                trailing = {
                    if (row.text == null) {
                        Text(stringResource(R.string.converter_row_no_rate), color = LocalStatusPalette.current.warning)
                    } else {
                        Text(
                            row.text,
                            style = RateConvertedStyle,
                            color = when {
                                state.isEmpty -> MaterialTheme.colorScheme.onSurfaceVariant
                                row.isSource -> accentInk()
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                        )
                    }
                },
                onClick = { actions.onSource(row.code) },
                containerColor = if (row.isSource) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
            )
        }
        CoShellHint(stringResource(R.string.converter_rows_hint))
    }
}
