package app.coshell.core.designsystem.category

/**
 * Category colours: 18 neon hues (owner's choice, design-questions §2) + 6 warm ones. `Category.color` stores
 * the neon value ([Swatch.stored], ARGB); on screen it is shown by theme — neon in the dark theme,
 * a deeper shade of the same hue in the light one (readable on white). See [display].
 *
 * Colours saved before the neon palette (Material colours) are not migrated in the database:
 * [display] and [normalize] map them to the swatch in the same place, so old data and the
 * other phone (sync) keep working.
 */
object CategoryPalette {
    /** One palette colour: [stored] — neon, in the database and dark theme; [light] — for the light theme. */
    data class Swatch(val stored: Long, val light: Long)

    val swatches: List<Swatch> = listOf(
        Swatch(0xFFFF5470, 0xFFC8203A), // red
        Swatch(0xFFFF4FD8, 0xFFB5168F), // pink
        Swatch(0xFFD36BFF, 0xFF8F24BF), // purple
        Swatch(0xFF9D7BFF, 0xFF6236E0), // violet
        Swatch(0xFF6E8BFF, 0xFF3049D1), // indigo
        Swatch(0xFF38B6FF, 0xFF0069A8), // blue
        Swatch(0xFF2BE4FF, 0xFF007785), // cyan
        Swatch(0xFF00E5C0, 0xFF00806B), // mint
        Swatch(0xFF4DFF88, 0xFF0B7F3D), // green
        Swatch(0xFFA8FF3E, 0xFF4D7F00), // lime
        Swatch(0xFFF5FF3D, 0xFF6E7300), // yellow
        Swatch(0xFFFFD43B, 0xFF8A5F00), // amber
        Swatch(0xFFFFA53D, 0xFFA65300), // orange
        Swatch(0xFFFF7A3D, 0xFFC23F00), // flame
        Swatch(0xFFFFB38A, 0xFF9C4E26), // peach (was brown)
        Swatch(0xFF8FB3CC, 0xFF4A6B80), // steel
        Swatch(0xFFB0B8C4, 0xFF5F6874), // silver
        Swatch(0xFF7A8494, 0xFF454D5A), // graphite
        // Warm, softer hues (design-refactor §palette). Appended: legacy mapping zips by index.
        Swatch(0xFFFF8A65, 0xFFB8431F), // coral
        Swatch(0xFFE07A5F, 0xFFA3432A), // terracotta
        Swatch(0xFFFF8FA3, 0xFFB0304A), // rose
        Swatch(0xFFFFC46B, 0xFF8F5A00), // honey
        Swatch(0xFFD9A066, 0xFF8A5A24), // caramel
        Swatch(0xFFF2D0A4, 0xFF7D5A2E), // sand
    )

    /** Stored values offered in the category editor. */
    val colors: List<Long> = swatches.map { it.stored }

    /** Material colours of the pre-neon palette, in the same order as [swatches]. */
    private val legacy: List<Long> = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF00BCD4, 0xFF009688,
        0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39, 0xFFFFC107,
        0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF607D8B,
        0xFF9E9E9E, 0xFF757575,
    )

    private val byStored: Map<Long, Swatch> =
        swatches.associateBy { it.stored } + legacy.zip(swatches).toMap()

    /** Palette value for [color]: a pre-neon colour becomes its neon swatch; anything else is kept. */
    fun normalize(color: Long): Long = byStored[color]?.stored ?: color

    /** ARGB to draw [color] with in the given theme; colours outside the palette are drawn as stored. */
    fun display(color: Long, dark: Boolean): Long {
        val swatch = byStored[color] ?: return color
        return if (dark) swatch.stored else swatch.light
    }
}
