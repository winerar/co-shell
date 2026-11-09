package app.coshell.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(current = screen, onTab = { navController.navigateToTab(it) }, onAdd = onAdd)
            }
        },
    ) { padding ->
        // Inner screens have their own Scaffold and handle the status bar themselves, so only the
        // bottom inset is applied and consumed here; consuming all of `padding` would hide the top one.
        val bottomInset = PaddingValues(bottom = padding.calculateBottomPadding())
        Box(Modifier.padding(bottomInset).consumeWindowInsets(bottomInset)) {
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                enterTransition = { screenEnter() },
                exitTransition = { screenExit() },
                popEnterTransition = { screenPopEnter() },
                popExitTransition = { screenPopExit() },
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
