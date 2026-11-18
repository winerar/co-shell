package app.coshell.core.designsystem.components

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * «Настройки → Внешний вид → Плотность списков»: `true` — the data lists («Операции», «Счета») use compact rows.
 * Set once over the whole app; a list row passes it to [CoShellListRow] as `compact`.
 */
val LocalCompactLists = staticCompositionLocalOf { false }
