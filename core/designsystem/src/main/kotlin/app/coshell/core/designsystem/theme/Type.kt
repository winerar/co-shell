package app.coshell.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val Default = Typography()

val CoShellTypography = Typography(
    // Big balance numbers: tabular figures keep digits aligned while they change.
    displayMedium = Default.displayMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum",
    ),
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.SemiBold),
)

/** Style for amounts in lists. */
val AmountTextStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.Medium,
    fontFeatureSettings = "tnum",
    textAlign = TextAlign.End,
)
