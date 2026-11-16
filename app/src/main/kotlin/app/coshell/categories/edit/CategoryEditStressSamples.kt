package app.coshell.categories.edit

/**
 * Stress sample of the category editor (#378): a name of 50+ characters, a folder with a long name among ten
 * folders.
 */
internal val categoryEditStressSample =
    CategoryEditUiState(
        isNew = false,
        name = "Коммунальные платежи и обслуживание квартиры в Буэнос-Айресе",
        icon = "fuel",
        color = 0xFFFFC107,
        typeLocked = true,
        parentId = "f0",
        folders = listOf("Машина и всё, что с ней связано: бензин, парковка, страховка", "Дом", "Еда", "Дети", "Здоровье", "Отдых", "Подарки", "Работа", "Связь", "Налоги")
            .mapIndexed { i, name -> CategoryEditUiState.Option("f$i", name) },
    )
