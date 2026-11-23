package app.coshell.ui.bar

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.components.LocalBottomBarInset

/**
 * Lifts a screen's own button at the bottom (`Scaffold(bottomBar = …)`: «Обработать», «Записать», «Обновить») above
 * the bottom capsule on tabs and above the system navigation bar on inner screens (#640): there the capsule is gone,
 * [LocalBottomBarInset] is 0 and the band of navigation buttons would cover the button. On tabs the shell has already
 * consumed the navigation bar, so `navigationBarsPadding` adds nothing there.
 */
@Composable
fun Modifier.aboveBottomBars(): Modifier = padding(bottom = LocalBottomBarInset.current).navigationBarsPadding()
