package app.coshell.core.designsystem.category

/**
 * Colours offered in the category editor (ARGB, as stored in `Category.color`). Mid-tone
 * Material colours: readable as an icon tint on both light and dark backgrounds.
 */
object CategoryPalette {
    val colors: List<Long> = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF00BCD4, 0xFF009688,
        0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39, 0xFFFFC107,
        0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF607D8B,
        0xFF9E9E9E, 0xFF757575,
    )
}
