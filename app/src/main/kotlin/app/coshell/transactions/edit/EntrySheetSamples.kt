package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import kotlinx.datetime.LocalDate

// Worst case of board `Fb290A` (#290) for previews and screenshots: receipt in another currency, tags, a note and
// 24 categories in folders — and still no scroll on a phone.

private fun cat(id: String, name: String, icon: String, color: Long) = CategoryItem(id, name, icon, color)

internal val sampleCategories24 = listOf(
    CategoryGroup(
        cat("food", "Еда", "food", 0xFFA8FF3E),
        listOf(cat("shop", "Продукты", "food", 0xFFA8FF3E), cat("cafe", "Кафе", "cafe", 0xFFFFA53D), cat("bar", "Бар", "bar", 0xFFFF4FD8)),
    ),
    CategoryGroup(
        cat("house", "Дом и быт", "home", 0xFF2BE4FF),
        listOf(
            cat("rent", "Аренда", "home", 0xFF2BE4FF), cat("utilities", "Свет и газ", "utilities", 0xFFFFD43B),
            cat("phone", "Связь", "phone", 0xFFD36BFF), cat("internet", "Интернет", "internet", 0xFF9D7BFF),
            cat("cleaning", "Уборка", "cleaning", 0xFF00E5C0), cat("repair", "Ремонт", "repair", 0xFFFFC46B),
            cat("furniture", "Мебель", "furniture", 0xFFD9A066), cat("pets", "Кот", "pets", 0xFFFF8A65),
        ),
    ),
    CategoryGroup(
        cat("me", "Личное", "beauty", 0xFFFF8FA3),
        listOf(
            cat("clothes", "Одежда", "clothes", 0xFFFF8FA3), cat("sport", "Спорт", "sport", 0xFF4DFF88),
            cat("beauty", "Красота", "beauty", 0xFFFF4FD8), cat("pharmacy", "Аптека", "pharmacy", 0xFFFF5470),
            cat("books", "Книги", "books", 0xFFF2D0A4), cat("education", "Школа", "education", 0xFFF5FF3D),
        ),
    ),
    CategoryGroup(cat("taxi", "Такси", "taxi", 0xFFF5FF3D), emptyList()),
    CategoryGroup(cat("transport", "Транспорт", "transport", 0xFF38B6FF), emptyList()),
    CategoryGroup(cat("movies", "Кино", "movies", 0xFF00E5C0), emptyList()),
    CategoryGroup(cat("travel", "Путешествия", "travel", 0xFF9FD8FF), emptyList()),
    CategoryGroup(cat("electronics", "Техника", "electronics", 0xFF8FB3CC), emptyList()),
    CategoryGroup(cat("kids", "Дети", "kids", 0xFFFFD43B), emptyList()),
)

private val sampleAccounts = listOf(
    AccountOption("usdt", "USDT-карта", "USDT"),
    AccountOption("ars", "Карта", "ARS", fractionDigits = 2),
)

private val sampleToday = LocalDate(2026, 11, 13)

/** 13,10 USDT for a 13 300 ARS receipt in «Кафе», a note and two tags — the board's sheet. */
internal val entrySheetSample: TransactionEditUiState = TransactionEditUiState(
    amount = "13,10",
    accounts = sampleAccounts,
    accountId = "usdt",
    categories = sampleCategories24,
    categoryId = "cafe",
    frequentCategories = frequentCategories(sampleCategories24, listOf("shop", "cafe", "taxi", "pharmacy", "rent", "phone", "movies"), "cafe"),
    date = sampleToday,
    today = sampleToday,
    note = "Café Martínez, с Аней",
    receiptEnabled = true,
    receiptCurrency = "ARS",
    receiptAmount = "13 300",
    receiptCurrencies = listOf("ARS", "USD", "EUR"),
    keypadTarget = TransactionEditUiState.KeypadTarget.RECEIPT,
    tagIds = listOf("аня", "на двоих"),
    tags = TagChoices(
        listOf(TagOption("аня", "Аня", 0xFFFF6FB5L, 61), TagOption("на двоих", "на двоих", 0xFFB8F33AL, 30)),
        recentIds = listOf("аня", "на двоих"),
    ),
)
