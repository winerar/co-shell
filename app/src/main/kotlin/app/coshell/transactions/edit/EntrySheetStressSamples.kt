package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem

/**
 * Stress sample of the entry sheet (#378): billion-sized amounts in two currencies, an account and a category with
 * names of 50+ characters, a note of three lines, tags with long names and ten receipt currencies.
 */
internal val entrySheetStressSample: TransactionEditUiState = run {
    val longCategory = CategoryItem("long", "Коммунальные платежи и обслуживание квартиры в Буэнос-Айресе", "home", 0xFF2BE4FF)
    val categories = listOf(CategoryGroup(longCategory, emptyList())) + sampleCategories24
    val longTag = "Продуктыиздомашнегомагазинабезпробеловвообще"
    entrySheetSample.copy(
        amount = "999 999 999,99",
        accounts = listOf(
            AccountOption("long", "Тинькофф Накопительный Анастасии для отпуска в Патагонии", "USDT"),
            AccountOption("ars", "Карта", "ARS", fractionDigits = 2),
        ),
        accountId = "long",
        categories = categories,
        categoryId = "long",
        frequentCategories = frequentCategories(categories, listOf("long", "shop", "cafe"), "long"),
        note = "Café Martínez с Аней и Борей, потом ещё заехали в аптеку и на рынок за фруктами, а вечером ужин у родителей",
        receiptAmount = "999 999 999 999",
        receiptCurrencies = listOf("ARS", "USD", "EUR", "AMD", "RUB", "JPY", "KWD", "BTC", "BRL", "CLP"),
        tagIds = listOf("аня", longTag),
        tags = TagChoices(
            listOf(TagOption("аня", "Аня", 0xFFFF6FB5L, 61), TagOption(longTag, longTag, 0xFFB8F33AL, 30)),
            recentIds = listOf("аня", longTag),
        ),
    )
}
