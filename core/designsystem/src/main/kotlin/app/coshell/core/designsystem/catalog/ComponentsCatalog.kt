package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing

/** Showcase of the base components (fields, rows, blocks) for Android Studio previews. */
@Composable
fun ComponentsCatalog() {
    Column(Modifier.padding(vertical = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Column(Modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellTextField(value = "Карта", onValueChange = {}, label = "Название")
            CoShellTextField(value = "US", onValueChange = {}, label = "Код валюты", error = "Три латинские буквы")
            CoShellAmountField(value = "1 049,69", onValueChange = {}, label = "Сумма", currency = "USD", hint = "Списано со счёта")
            CoShellSection("Тип", error = null) { CoShellHint("Тип папки — тип её подкатегорий") }
            CoShellMessage("Копия сохранена", isError = false)
            CoShellMessage("Нет сети", isError = true)
            CoShellButton(onClick = {}) { Text("Сохранить") }
        }
        CoShellCard(Modifier.padding(horizontal = Spacing.l)) {
            CoShellListRow(
                title = "Продукты",
                subtitle = "Карта · Миша",
                subtitleMaxLines = 1,
                leading = { CategoryBadge("food", CategoryPalette.colors.first()) },
                trailing = { AmountText("−12,50 USD", style = AmountTextStyle, color = LocalAmountPalette.current.expense) },
                onClick = {},
            )
            CoShellSwitchRow(title = "Учитывать в общем балансе", checked = true, onCheckedChange = {})
            CoShellListRow(title = "Резервная копия", subtitle = "Недоступно", enabled = false, onClick = {})
        }
    }
}

@Preview(name = "Components · light", showBackground = true)
@Composable
private fun ComponentsCatalogLightPreview() = CoShellTheme(darkTheme = false) { Surface { ComponentsCatalog() } }

@Preview(name = "Components · dark", showBackground = true)
@Composable
private fun ComponentsCatalogDarkPreview() = CoShellTheme(darkTheme = true) { Surface { ComponentsCatalog() } }

@Preview(name = "Dialog · dark")
@Composable
private fun DialogPreview() = CoShellTheme(darkTheme = true) {
    CoShellAlertDialog(
        title = "Удалить счёт?",
        text = "Счёт без операций удалится насовсем.",
        confirmLabel = "Удалить",
        onConfirm = {},
        onDismiss = {},
        dismissLabel = "Отмена",
        destructive = true,
    )
}
