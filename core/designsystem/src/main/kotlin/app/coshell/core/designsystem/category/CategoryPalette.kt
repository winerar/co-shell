package app.coshell.core.designsystem.category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Category, account and tag colours (1.0, owner's choice 2026-11-16, #105): 24 colours, the same fill in both themes, dark
 * ink on each (≥ 4.6:1), any two at ΔE2000 ≥ 16.5. A pale fill that reads under 3:1 on its surface gets a thin ring of its
 * own deeper shade ([ring]). Values and the audit — scripts/agent/themes_1_0.py.
 *
 * Older colours are not migrated in the database (sync with the other phone keeps working): the 36 colours of 0.x and the
 * Material colours before them are shown as the nearest of the 24 ([display]) and picked as it ([normalize]).
 */
object CategoryPalette {
    /** Stored values offered in the colour pickers, in the order of the picker grid (warm → cold → pink). */
    val colors: List<Long> = listOf(
        RED,
        CORAL,
        ORANGE,
        PEACH,
        CARAMEL,
        AMBER,
        SAND,
        YELLOW,
        LIME,
        SAGE,
        GREEN,
        SEAFOAM,
        TEAL,
        CYAN,
        SKY,
        STEEL,
        PERIWINKLE,
        INDIGO,
        VIOLET,
        LAVENDER,
        MAGENTA,
        MAUVE,
        PINK,
        BLUSH,
    )

    private const val RED = 0xFFFF2039L
    private const val CORAL = 0xFFFF8D85L
    private const val ORANGE = 0xFFFF7A00L
    private const val PEACH = 0xFFFFD4BAL
    private const val CARAMEL = 0xFF9E7756L
    private const val AMBER = 0xFFE0A800L
    private const val SAND = 0xFFBCB38FL
    private const val YELLOW = 0xFFF2E640L
    private const val LIME = 0xFF95C11FL
    private const val SAGE = 0xFF759273L
    private const val GREEN = 0xFF0EEF82L
    private const val SEAFOAM = 0xFFAEDFD9L
    private const val TEAL = 0xFF00C2A2L
    private const val CYAN = 0xFF08CEFEL
    private const val SKY = 0xFF019DFDL
    private const val STEEL = 0xFF9DADBBL
    private const val PERIWINKLE = 0xFFD7D9FFL
    private const val INDIGO = 0xFF6077DBL
    private const val VIOLET = 0xFFB15FFEL
    private const val LAVENDER = 0xFFE5A6FDL
    private const val MAGENTA = 0xFFE501B6L
    private const val MAUVE = 0xFF9E7087L
    private const val PINK = 0xFFFF79B4L
    private const val BLUSH = 0xFFFECBDCL

    /** 0.x colours (36 neon/warm/pastel, then the Material ones before them) → the nearest of [colors]. */
    private val legacy: Map<Long, Long> = mapOf(
        0xFFFF5470L to RED, // red
        0xFFFF4FD8L to MAGENTA, // pink
        0xFFD36BFFL to VIOLET, // purple
        0xFF9D7BFFL to VIOLET, // violet
        0xFF6E8BFFL to INDIGO, // indigo
        0xFF38B6FFL to SKY, // blue
        0xFF2BE4FFL to CYAN, // cyan
        0xFF00E5C0L to TEAL, // mint
        0xFF4DFF88L to GREEN, // green
        0xFFA8FF3EL to LIME, // lime
        0xFFF5FF3DL to YELLOW, // yellow
        0xFFFFD43BL to AMBER, // amber
        0xFFFFA53DL to AMBER, // orange
        0xFFFF7A3DL to ORANGE, // flame
        0xFFFFB38AL to PEACH, // peach (was brown)
        0xFF8FB3CCL to STEEL, // steel
        0xFFB0B8C4L to STEEL, // silver
        0xFF7A8494L to STEEL, // graphite
        0xFFFF8A65L to CORAL, // coral
        0xFFE07A5FL to CORAL, // terracotta
        0xFFFF8FA3L to PINK, // rose
        0xFFFFC46BL to AMBER, // honey
        0xFFD9A066L to CARAMEL, // caramel
        0xFFF2D0A4L to PEACH, // sand
        0xFFC9B6FFL to LAVENDER, // lavender
        0xFF9FD8FFL to CYAN, // sky
        0xFF9BF5D6L to SEAFOAM, // seafoam
        0xFFF4F59AL to YELLOW, // lemon cream
        0xFFFFB3D1L to BLUSH, // blush
        0xFFE89BFFL to LAVENDER, // orchid
        0xFFD6F5FFL to SEAFOAM, // ice
        0xFFDFFF00L to YELLOW, // chartreuse
        0xFF7A7AFFL to INDIGO, // ultramarine
        0xFFB5C23AL to LIME, // olive
        0xFFC9B27CL to SAND, // khaki
        0xFF3DDC97L to GREEN, // jade
        0xFFF44336L to RED,
        0xFFE91E63L to PINK,
        0xFF9C27B0L to VIOLET,
        0xFF673AB7L to VIOLET,
        0xFF3F51B5L to INDIGO,
        0xFF2196F3L to SKY,
        0xFF00BCD4L to CYAN,
        0xFF009688L to TEAL,
        0xFF4CAF50L to GREEN,
        0xFF8BC34AL to LIME,
        0xFFCDDC39L to YELLOW,
        0xFFFFC107L to AMBER,
        0xFFFF9800L to ORANGE,
        0xFFFF5722L to ORANGE,
        0xFF795548L to CARAMEL,
        0xFF607D8BL to STEEL,
        0xFF9E9E9EL to STEEL,
        0xFF757575L to STEEL,
    )

    /** Palette value for [color]: an older colour becomes the nearest of the 24; anything else is kept. */
    fun normalize(color: Long): Long = legacy[color] ?: color

    /**
     * A palette colour picked from [key] (a tag id): the same on every phone and every run — FNV-1a, not
     * `String.hashCode`, so the rule does not depend on the platform. Used where no colour was chosen (#129).
     */
    fun forKey(key: String): Long {
        var hash = 0x811C9DC5L
        key.forEach { ch ->
            hash = hash xor ch.code.toLong()
            hash = (hash * 0x01000193L) and 0xFFFFFFFFL
        }
        return colors[(hash % colors.size).toInt()]
    }

    /** ARGB to draw [color] with: the same in both themes since 1.0; an older colour as the nearest of the 24. */
    @Suppress("UNUSED_PARAMETER")
    fun display(color: Long, dark: Boolean): Long = normalize(color)

    /**
     * Ring for a fill that reads under 3:1 on [surface] (peach, yellow, periwinkle… on white): the same colour moved
     * towards black (light surface) or white (dark) until it reaches 3:1; `null` when the fill is visible by itself.
     */
    fun ring(fill: Color, surface: Color): Color? {
        if (contrast(fill, surface) >= RING_TARGET) return null
        val toward = if (surface.luminance() > HALF) Color.Black else Color.White
        var k = 0f
        var c = fill
        while (contrast(c, surface) < RING_TARGET && k < 1f) {
            k += RING_STEP
            c = lerp(fill, toward, k)
        }
        return c
    }

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance() + 0.05f
        val lb = b.luminance() + 0.05f
        return maxOf(la, lb) / minOf(la, lb)
    }

    private const val RING_TARGET = 3f
    private const val RING_STEP = 0.05f
    private const val HALF = 0.5f
}
