package app.coshell.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val Default = Typography()

private fun TextStyle.onest(weight: FontWeight? = null) =
    copy(fontFamily = TextFontFamily, fontWeight = weight ?: fontWeight)

/**
 * Text styles for words: the Material 3 size scale set in Onest. Headings are SemiBold,
 * labels (buttons, tabs, chips) Medium, body text Regular. Amounts and rates use the
 * `Amount*Style` / [RateTextStyle] below, never these.
 */
val CoShellTypography = Typography(
    displayLarge = Default.displayLarge.onest(),
    displayMedium = Default.displayMedium.onest(),
    displaySmall = Default.displaySmall.onest(),
    headlineLarge = Default.headlineLarge.onest(FontWeight.SemiBold),
    headlineMedium = Default.headlineMedium.onest(FontWeight.SemiBold),
    headlineSmall = Default.headlineSmall.onest(FontWeight.SemiBold),
    titleLarge = Default.titleLarge.onest(FontWeight.SemiBold),
    titleMedium = Default.titleMedium.onest(FontWeight.Medium),
    titleSmall = Default.titleSmall.onest(FontWeight.Medium),
    bodyLarge = Default.bodyLarge.onest(),
    bodyMedium = Default.bodyMedium.onest(),
    bodySmall = Default.bodySmall.onest(),
    labelLarge = Default.labelLarge.onest(FontWeight.Medium),
    labelMedium = Default.labelMedium.onest(FontWeight.Medium),
    labelSmall = Default.labelSmall.onest(FontWeight.Medium),
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

/** Exchange rates («1 USD = 1 234,5 ARS»): same monospace digits as amounts, list-row size. */
val RateTextStyle = TextStyle(
    fontFamily = NumbersFontFamily,
    fontSize = 15.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Normal,
)
