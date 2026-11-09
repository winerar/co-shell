package app.coshell.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onPlaced
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import app.coshell.core.designsystem.components.CoShellSnackbarHost
import app.coshell.core.designsystem.motion.ConfirmPulse
import app.coshell.core.designsystem.motion.LocalAddConfirm
import app.coshell.core.designsystem.motion.PressOrigin

/**
 * The app's frame: the bottom bar on the tabs, the snackbar and the [NavHost] with [graph]. No Hilt
 * here, so the smoke test (`MainShellSmokeTest`) runs this very frame with real tab screens.
 */
@Composable
internal fun MainShell(
    navController: NavHostController,
    snackbar: SnackbarHostState,
    onAdd: () -> Unit,
    graph: NavGraphBuilder.() -> Unit,
) {
    // A sheet (dialog destination) opens over a screen: the bar under it stays as it was.
    val visible by navController.visibleEntries.collectAsStateWithLifecycle()
    val screen = visible.lastOrNull { it.destination !is DialogNavigator.Destination && it.destination !is NavGraph }
        ?.destination
    val showBottomBar = TopLevel.entries.any { screen.isOn(it) }
    // While the bar rides away under an inner screen it keeps showing the tab it was opened from.
    var barScreen by remember { mutableStateOf(screen) }
    if (showBottomBar) barScreen = screen
    // Screens grow from the tapped button or row and shrink back into it.
    val origins = remember { ScreenOrigins(PressOrigin) }
    // An editor's save makes the «+» answer with a check mark (design-refactor Step 4).
    val addConfirm = remember { ConfirmPulse() }

    CompositionLocalProvider(LocalAddConfirm provides addConfirm) {
        Shell(navController, snackbar, onAdd, graph, barScreen, showBottomBar, origins)
    }
}

@Composable
private fun Shell(
    navController: NavHostController,
    snackbar: SnackbarHostState,
    onAdd: () -> Unit,
    graph: NavGraphBuilder.() -> Unit,
    barScreen: NavDestination?,
    showBottomBar: Boolean,
    origins: ScreenOrigins,
) {
    Scaffold(
        snackbarHost = { CoShellSnackbarHost(snackbar) },
        bottomBar = {
            MainBottomBar(
                current = barScreen,
                onTab = { navController.navigateToTab(it) },
                onAdd = onAdd,
                visible = showBottomBar,
            )
        },
    ) { padding ->
        // Inner screens have their own Scaffold and handle the status bar themselves, so only the
        // bottom inset is applied and consumed here; consuming all of `padding` would hide the top one.
        val bottomInset = PaddingValues(bottom = padding.calculateBottomPadding())
        Box(
            Modifier
                .padding(bottomInset)
                .consumeWindowInsets(bottomInset)
                .onPlaced { origins.hostBounds = it.boundsInWindow() },
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                enterTransition = { enter(origins) },
                exitTransition = { exit(origins) },
                popEnterTransition = { popEnter(origins) },
                popExitTransition = { popExit(origins) },
                builder = graph,
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopLevel) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
