package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// The colours of each CoShellPalette (1.0 themes, owner's choice 2026-11-16, #354/#385). Generated from
// scripts/agent/themes_1_0.py — change both together; that script audits every pair in both themes. Schemes: ColorSchemes.kt.
// inkLight — the deep shade of the primary for the light theme (owner's poll 2026-11-23, #493 Б): ≥ 4.5:1 on the light card,
// scripts/agent/themes_493_inks.py; soft — a pastel theme (Мате, Сакура, Лотос: #493 «нежные — А», no extra brightness).
// warning* — the theme's attention colour (owner's poll 2026-11-20, Б: the hue farthest from the theme's own colours), scripts/agent/alert_colors.py.

// Лайм — неон, кислотный лайм + фиолетовый
internal val LimeSpec = PaletteSpec(
    primary = Color(0xFFB8FF2E),
    onPrimary = Color(0xFF0E1F00),
    accentDark = Color(0xFFC27BFF),
    accentLight = Color(0xFF7A2BD6),
    tertiaryDark = Color(0xFFFFE14D),
    tertiaryLight = Color(0xFF6E6200),
    incomeDark = Color(0xFF2BE4FF),
    incomeLight = Color(0xFF016D86),
    expenseDark = Color(0xFFFF4D6D),
    expenseLight = Color(0xFFC60B3D),
    warningDark = Color(0xFF07B47C),
    warningLight = Color(0xFF05724D),
    backgroundDark = Color(0xFF050805),
    backgroundLight = Color(0xFFF7FFEA),
    inkLight = Color(0xFF587E05),
)

// Гранат — неон, рубиновый + бирюзовый
internal val GarnetSpec = PaletteSpec(
    primary = Color(0xFFFF3D6E),
    onPrimary = Color(0xFF1F0009),
    accentDark = Color(0xFF2EE6BE),
    accentLight = Color(0xFF056E59),
    tertiaryDark = Color(0xFFFFC94D),
    tertiaryLight = Color(0xFF7A5600),
    incomeDark = Color(0xFF5CC8FF),
    incomeLight = Color(0xFF036797),
    expenseDark = Color(0xFFFF9466),
    expenseLight = Color(0xFFAB3903),
    warningDark = Color(0xFFBC73F1),
    warningLight = Color(0xFF8136B1),
    backgroundDark = Color(0xFF12060A),
    backgroundLight = Color(0xFFFFF4F6),
    inkLight = Color(0xFFD0024E),
)

// Банан — неон, жёлтый + индиго
internal val BananaSpec = PaletteSpec(
    primary = Color(0xFFFFE135),
    onPrimary = Color(0xFF2A2200),
    accentDark = Color(0xFF9D8CFF),
    accentLight = Color(0xFF4B3BCF),
    tertiaryDark = Color(0xFFFF9F5C),
    tertiaryLight = Color(0xFFA54704),
    incomeDark = Color(0xFF5CF08A),
    incomeLight = Color(0xFF027235),
    expenseDark = Color(0xFFFF5C7A),
    expenseLight = Color(0xFFC60B3D),
    warningDark = Color(0xFF04ADC2),
    warningLight = Color(0xFF076E7C),
    backgroundDark = Color(0xFF0A0903),
    backgroundLight = Color(0xFFFFFBE5),
    inkLight = Color(0xFF837202),
)

// Мате — пастель, травяной + медный
internal val MateSpec = PaletteSpec(
    primary = Color(0xFFB5D98A),
    onPrimary = Color(0xFF1A2A08),
    accentDark = Color(0xFFFFA77A),
    accentLight = Color(0xFFA1451A),
    tertiaryDark = Color(0xFFEAD88A),
    tertiaryLight = Color(0xFF6E5E00),
    incomeDark = Color(0xFF6BE0D8),
    incomeLight = Color(0xFF016D67),
    expenseDark = Color(0xFFFF7088),
    expenseLight = Color(0xFFBE1140),
    warningDark = Color(0xFF8B99FE),
    warningLight = Color(0xFF4F53D1),
    backgroundDark = Color(0xFF0B0D08),
    backgroundLight = Color(0xFFF6F8EC),
    inkLight = Color(0xFF5A7A2D),
    soft = true,
)

// Эспрессо — неон, кофейный + электрик
internal val EspressoSpec = PaletteSpec(
    primary = Color(0xFFC67F4D),
    onPrimary = Color(0xFF1F0C00),
    accentDark = Color(0xFF4DA6FF),
    accentLight = Color(0xFF1F5FA8),
    tertiaryDark = Color(0xFFEBC096),
    tertiaryLight = Color(0xFF7A4E1E),
    incomeDark = Color(0xFF7FE0B0),
    incomeLight = Color(0xFF006F47),
    expenseDark = Color(0xFFFF5C8A),
    expenseLight = Color(0xFFC0084C),
    warningDark = Color(0xFFC576F2),
    warningLight = Color(0xFF8837B1),
    backgroundDark = Color(0xFF110B08),
    backgroundLight = Color(0xFFFBF6F0),
    inkLight = Color(0xFF9C5925),
)

// Квас — неон, янтарь + бирюза
internal val KvasSpec = PaletteSpec(
    primary = Color(0xFFFFB020),
    onPrimary = Color(0xFF2A1700),
    accentDark = Color(0xFF3DDBD9),
    accentLight = Color(0xFF036D73),
    tertiaryDark = Color(0xFFFF7A5C),
    tertiaryLight = Color(0xFFAE3818),
    incomeDark = Color(0xFF9CF06B),
    incomeLight = Color(0xFF3B6E03),
    expenseDark = Color(0xFFFF5C7A),
    expenseLight = Color(0xFFC4063B),
    warningDark = Color(0xFFAD84FF),
    warningLight = Color(0xFF7644C5),
    backgroundDark = Color(0xFF0D0904),
    backgroundLight = Color(0xFFFFF8EB),
    inkLight = Color(0xFF956405),
)

// Сакура — пастель, розовый + листва
internal val SakuraSpec = PaletteSpec(
    primary = Color(0xFFFFB3CF),
    onPrimary = Color(0xFF3A0A1E),
    accentDark = Color(0xFF8EE6A0),
    accentLight = Color(0xFF0A7030),
    tertiaryDark = Color(0xFFC9B6FF),
    tertiaryLight = Color(0xFF5B3FD0),
    incomeDark = Color(0xFF7FD0FF),
    incomeLight = Color(0xFF00658A),
    expenseDark = Color(0xFFFF6B5C),
    expenseLight = Color(0xFFC0181A),
    warningDark = Color(0xFFCD9509),
    warningLight = Color(0xFF815C04),
    backgroundDark = Color(0xFF120A0E),
    backgroundLight = Color(0xFFFFF5F8),
    inkLight = Color(0xFF9E5A75),
    soft = true,
)

// Василёк — неон, синий + пшеничный
internal val CornflowerSpec = PaletteSpec(
    primary = Color(0xFF5C8DFF),
    onPrimary = Color(0xFF000B2A),
    accentDark = Color(0xFFFFCC4D),
    accentLight = Color(0xFF7A5600),
    tertiaryDark = Color(0xFFFF7A5C),
    tertiaryLight = Color(0xFFB33114),
    incomeDark = Color(0xFF6BF08F),
    incomeLight = Color(0xFF01712F),
    expenseDark = Color(0xFFFF5C8A),
    expenseLight = Color(0xFFC0084C),
    warningDark = Color(0xFF02A3B3),
    warningLight = Color(0xFF026771),
    backgroundDark = Color(0xFF060912),
    backgroundLight = Color(0xFFF4F7FF),
    inkLight = Color(0xFF3763D1),
)

// Лотос — пастель, сиреневый + лист лотоса
internal val LotusSpec = PaletteSpec(
    primary = Color(0xFFCDB4FF),
    onPrimary = Color(0xFF1E0A3A),
    accentDark = Color(0xFFC7F25C),
    accentLight = Color(0xFF4C6902),
    tertiaryDark = Color(0xFFFF8AC8),
    tertiaryLight = Color(0xFFAD2C6B),
    incomeDark = Color(0xFF5CE6FF),
    incomeLight = Color(0xFF05697F),
    expenseDark = Color(0xFFFF5C6E),
    expenseLight = Color(0xFFC3013A),
    warningDark = Color(0xFFE08303),
    warningLight = Color(0xFF915303),
    backgroundDark = Color(0xFF0E0814),
    backgroundLight = Color(0xFFFBF5FF),
    inkLight = Color(0xFF7B62A7),
    soft = true,
)
