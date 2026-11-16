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

/** Hero number of a card («8 503», капитал): amount digits at headline size, bold. */
val AmountHeadlineStyle = RateTextStyle.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)

/** Figure in a stat row («1 620» under «пришло»): amount digits at title-small size. */
val AmountStatStyle = RateTextStyle.copy(fontSize = 14.sp, lineHeight = 20.sp)

/** Cells of a small table of amounts (the capital breakdown): amount digits at body-small size. */
val AmountCellStyle = RateTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp)

/** [AmountCellStyle] for the total row. */
val AmountCellStrongStyle = AmountCellStyle.copy(fontWeight = FontWeight.SemiBold)

// Emphasis variants of the word styles (#367): screens take these instead of `.copy(fontWeight = …)`.

/** Tab-screen title under the top bar. */
val LargeTitleStyle = CoShellTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)

/** Title of an inner screen, a selection bar or a hero card. */
val ScreenTitleStyle = CoShellTypography.titleLarge.copy(fontWeight = FontWeight.Bold)

/** Dialog / block title in a warning colour. */
val TitleStrongStyle = CoShellTypography.titleMedium.copy(fontWeight = FontWeight.Bold)

/** Small block title (banner, option title). */
val TitleSmallStrongStyle = CoShellTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold)

/** Row title that must stand out (the red «Стереть данные» row). */
val BodyStrongStyle = CoShellTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)

/** Name on a template chip, header of a strip. */
val LabelStrongStyle = CoShellTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)

/** Initial inside an avatar. */
val LabelBoldStyle = CoShellTypography.labelLarge.copy(fontWeight = FontWeight.Bold)

/** Count on a category tile. */
val LabelMediumBoldStyle = CoShellTypography.labelMedium.copy(fontWeight = FontWeight.Bold)

/** «Все 52» / «+29» on icon and colour strips. */
val LabelMediumStrongStyle = CoShellTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold)

/** Caption on an icon strip. */
val LabelSmallStrongStyle = CoShellTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)

/** Rate under a total, caption size (12 sp, row line height). */
val RateSmallStyle = RateTextStyle.copy(fontSize = 12.sp)

/** Converted amount on a rate row. */
val RateConvertedStyle = RateTextStyle.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold)

/** Currency code in a chip. */
val RateChipStyle = RateTextStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)

/** Month total at the top of the review: [AmountTitleStyle] at 32 sp. */
val AmountReviewStyle = AmountTitleStyle.copy(fontSize = 32.sp, lineHeight = 40.sp)

/** Release version on a past-release card. */
val AmountTitleStrongStyle = AmountTitleStyle.copy(fontWeight = FontWeight.Bold)

/** The amount in a [app.coshell.core.designsystem.components.CoShellAmountSlot] of the entry sheet: [AmountTitleStyle] at 28 sp. */
val AmountSlotStyle = AmountTitleStyle.copy(fontSize = 28.sp, lineHeight = 36.sp)
