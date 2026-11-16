package app.coshell.categories.list

/**
 * Stress sample of «Категории» (#378): a group with a 40+ character name, a word without spaces, and 30 categories
 * inside one open group; names must end in «…» or wrap, never run under the chevron.
 */
internal val categoriesStressSample = CategoriesUiState(
    groups = listOf(
        CategoriesUiState.Group(
            CategoriesUiState.Item("home", "Коммунальные платежи и обслуживание квартиры", 0xFF8D6E63, "home"),
            children = listOf(
                CategoriesUiState.Item("w", "Электроэнергиягазводаиинтернетвместе", 0xFFFFC107, "bolt"),
                CategoriesUiState.Item("r", "Ремонт", null),
            ) + List(30) { CategoriesUiState.Item("c$it", "Категория номер ${it + 1}", 0xFF4CAF50, "food") },
            expanded = true,
        ),
        CategoriesUiState.Group(CategoriesUiState.Item("one", "Подарки", 0xFFFF9800, "redeem"), emptyList(), false),
    ),
    archived = listOf(CategoriesUiState.Item("old", "Старая категория с очень длинным названием из архива", null)),
)
