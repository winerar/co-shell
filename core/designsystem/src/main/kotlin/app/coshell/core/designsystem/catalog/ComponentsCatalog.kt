package app.coshell.core.designsystem.catalog

import app.coshell.core.designsystem.components.keypad.CoShellPinDots
import app.coshell.core.designsystem.components.keypad.CoShellPinPad
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.width
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellAvatar
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellDot
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.ButtonTone
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellCounter
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.SectionTone
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing

/** Showcase of the base components (fields, rows, blocks) for Android Studio previews. */
@Composable
fun ComponentsCatalog() {
    Column(Modifier.padding(vertical = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Column(Modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellSectionLabel("Тема")
            CoShellSectionLabel("Сегодня", tone = SectionTone.Accent)
            CoShellSearchField(value = "", onValueChange = {}, placeholder = "Поиск: заметка, сумма, категория", clearLabel = "Очистить")
            CoShellSearchField(value = "ga", onValueChange = {}, placeholder = "Поиск счёта", clearLabel = "Очистить")
            CoShellTextField(value = "Карта", onValueChange = {}, label = "Название")
            CoShellTextField(value = "US", onValueChange = {}, label = "Код валюты", error = "Три латинские буквы")
            CoShellAmountField(value = "1 049,69", onValueChange = {}, label = "Сумма", currency = "USD", hint = "Списано со счёта")
            CoShellSection("Тип", error = null) { CoShellHint("Тип подкатегорий — как у родительской") }
            CoShellMessage("Копия сохранена", isError = false)
            CoShellMessage("Нет сети", isError = true)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                CoShellButton(onClick = {}) { Text("Сохранить") }
                CoShellButton(onClick = {}, tone = ButtonTone.Tonal) { Text("Провести") }
            }
            SegmentedSample()
            CounterSample()
            MembersSample()
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
        Column(Modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellSectionLabel("Общее")
            CoShellRowGroup {
                row { CoShellListRow(title = "Валюты итогов", subtitle = "Основная: USD", onClick = {}) }
                row { CoShellSwitchRow(title = "Встряхнуть — новая операция", checked = true, onCheckedChange = {}) }
                row { CoShellListRow(title = "Обновление приложения", subtitle = "Версия 1.43 · последняя", onClick = {}) }
            }
            CoShellSectionLabel("Деньги")
            CoShellRowGroup {
                row { CoShellMenuRow(R.drawable.ic_category, "Категории", {}, subtitle = "Подкатегории, иконки и цвета") }
                row { CoShellMenuRow(R.drawable.ic_currency_exchange, "Курсы валют", {}, subtitle = "Обновляются сами раз в день") }
            }
        }
    }
}

/** Check marks of multi-choice sheets (#141): off, on, some of a folder — and filter chips beside them. */
@Composable
fun CheckMarkSample() {
    Row(Modifier.padding(Spacing.l), horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
        CoShellCheckMark(ToggleableState.Off)
        CoShellCheckMark(ToggleableState.On)
        CoShellCheckMark(ToggleableState.Indeterminate)
        CoShellFilterChip(selected = true, onClick = {}, label = { Text("Еда · 3") })
        CoShellFilterChip(selected = false, onClick = {}, label = { Text("Счёт") })
    }
}

/** #145: the PIN dots (typed, error) and the PIN pad with the fingerprint key. */
@Composable
fun PinSample() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l), horizontalAlignment = Alignment.CenterHorizontally) {
        CoShellPinDots(filled = 2, description = "Введено 2 из 4")
        CoShellPinDots(filled = 4, description = "PIN не подошёл", error = true)
        CoShellPinPad(onKey = {}, backspaceLabel = "Стереть", biometricLabel = "Отпечаток")
    }
}

/** К3: the neon pill for a choice (type of operation), the violet one for a view mode (grouping of accounts). */
@Composable
fun SegmentedSample() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellSegmented(listOf("Расход", "Доход", "Перевод"), selected = 0, onSelect = {})
        CoShellSegmented(listOf("По типу", "По валюте", "Чьи"), selected = 1, onSelect = {})
        // Too narrow for the words: the icons instead (#321).
        CoShellSegmented(
            listOf("Расход", "Доход", "Перевод"), selected = 0, onSelect = {}, modifier = Modifier.width(Sizes.tileMinHeight),
            icons = listOf(R.drawable.ic_call_made, R.drawable.ic_call_received, R.drawable.ic_swap_horiz),
        )
    }
}

/** #777: the counter of «Другое…» in «Уведомления» — time in the amount font, days with the lower end reached. */
@Composable
fun CounterSample() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellCounter("7:30", onStep = {}, minusLabel = "Раньше", plusLabel = "Позже", caption = "шаг 15 минут", mono = true)
        CoShellCounter("В тот же день", onStep = {}, minusLabel = "Меньше", plusLabel = "Больше", caption = "до платежа", canMinus = false)
    }
}

/** #367 А: member avatars, colour dots of a legend, status labels and the divider. */
@Composable
fun MembersSample() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            CoShellAvatar("Аня", categoryColor(CategoryPalette.colors[18]))
            CoShellAvatar("Миша", categoryColor(CategoryPalette.colors[13]))
            CoShellDot(LocalAmountPalette.current.income)
            Text("пришло", style = MaterialTheme.typography.labelSmall)
            CoShellDot(LocalAmountPalette.current.expense)
            Text("ушло", style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            CoShellStatusChip("сегодня", StatusTone.New)
            CoShellStatusChip("пропущен", StatusTone.Work)
            CoShellStatusChip("Готово", StatusTone.Done)
            CoShellStatusChip("Отклонено", StatusTone.No)
        }
        CoShellDivider()
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

@Preview(name = "Bottom bar · dark")
@Composable
private fun BottomBarPreview() = CoShellTheme(darkTheme = true) {
    CoShellBottomBar(
        tabs = listOf(
            CoShellTab(R.drawable.ic_home, "Обзор", selected = true),
            CoShellTab(R.drawable.ic_receipt_long, "Операции", selected = false),
            CoShellTab(R.drawable.ic_account_balance_wallet, "Счета", selected = false),
            CoShellTab(R.drawable.ic_more_horiz, "Ещё", selected = false),
        ),
        onTab = {},
        addLabel = "Новая операция",
        onAdd = {},
    )
}

@Preview(name = "Sheet content · light", showBackground = true)
@Composable
private fun SheetLayoutPreview() = CoShellTheme(darkTheme = false) {
    Surface {
        CoShellSheetLayout(title = "Новый счёт", actionLabel = "Сохранить", onAction = {}) {
            CoShellTextField(value = "", onValueChange = {}, label = "Название")
            CoShellAmountField(value = "", onValueChange = {}, label = "Начальный баланс", currency = "USD")
        }
    }
}

/** All palettes four in a row, as in «Внешний вид», in each palette's own colours. */
@Composable
private fun PalettesRow(selected: CoShellPalette) {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellPalette.entries.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                row.forEach { palette ->
                    CoShellPaletteSwatch(palette, palette.name, selected = palette == selected, onClick = {})
                }
            }
        }
    }
}

@Preview(name = "Palettes · light", showBackground = true)
@Composable
private fun PalettesLightPreview() = CoShellTheme(darkTheme = false) { Surface { PalettesRow(CoShellPalette.Lime) } }

@Preview(name = "Palettes · dark · Mango", showBackground = true)
@Composable
private fun PalettesDarkPreview() = CoShellTheme(darkTheme = true, palette = CoShellPalette.Kvas) {
    Surface { PalettesRow(CoShellPalette.Kvas) }
}
