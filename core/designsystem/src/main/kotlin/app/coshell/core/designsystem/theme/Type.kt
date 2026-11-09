package app.coshell.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val Default = Typography()

/** Text styles for words. Body font is still the system one until the owner picks it (ROADMAP). */
val CoShellTypography = Typography(
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.SemiBold),
)

// Amounts: JetBrains Mono, so digits line up and do not jump while a number changes.

/** Main total on «Обзор». Long values shrink with auto-size, so the size here is the maximum. */
val AmountHeroStyle = TextStyle(
    fontFamily = NumbersFontFamily,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    fontWeight = FontWeight.SemiBold,
)

/** Secondary totals (other report currencies, month spending). */
val AmountTitleStyle = TextStyle(
    fontFamily = NumbersFontFamily,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.Medium,
)

/** Amount being typed in a form field. */
val AmountInputStyle = TextStyle(
    fontFamily = NumbersFontFamily,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.End,
)

/** Amounts in list rows, right-aligned. */
val AmountTextStyle = TextStyle(
    fontFamily = NumbersFontFamily,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.End,
)
