package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.components.CoShellConfirmCard
import app.coshell.core.designsystem.components.CoShellDayGrid
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellModalSheetLayout
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Modal windows (#345, variant В; mockups `FbModalADay`, `FbModalBConfirm`): the question card and the sheet's
 * inside with the day grid — drawn without their windows, as references for the screenshot test.
 */
@Composable
fun ModalsCatalog() {
    Column(Modifier.padding(vertical = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        CoShellConfirmCard(
            title = "Удалить счёт «Galicia»?",
            text = "148 операций уйдут в корзину вместе со счётом.",
            confirmLabel = "Удалить",
            onConfirm = {},
            dismissLabel = "Отмена",
            destructive = true,
            modifier = Modifier.padding(horizontal = Spacing.l + Spacing.xs),
        )
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            CoShellModalSheetLayout(
                title = "Начало месяца",
                subtitle = "С какого числа считать бюджет — удобно в день зарплаты",
                closeLabel = "Закрыть",
                onClose = {},
                actionLabel = "Готово",
                modifier = Modifier.padding(top = Spacing.m),
            ) {
                CoShellDayGrid(selected = 10, onSelect = {}, markedDay = 1)
                CoShellHint("Точка — по умолчанию, календарный месяц.")
            }
        }
    }
}

@Preview(name = "Modals · dark", showBackground = true)
@Composable
private fun ModalsDarkPreview() = CoShellTheme(darkTheme = true) { Surface { ModalsCatalog() } }

@Preview(name = "Modals · light", showBackground = true)
@Composable
private fun ModalsLightPreview() = CoShellTheme(darkTheme = false) { Surface { ModalsCatalog() } }
