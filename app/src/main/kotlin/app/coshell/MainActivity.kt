package app.coshell

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.family.sync.ForegroundSync
import app.coshell.navigation.CoShellNavHost
import app.coshell.theme.ThemeSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var foregroundSync: ForegroundSync

    private val themeSettings: ThemeSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Family sync while the app is visible; stops in the background (WorkManager takes over).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { coroutineScope { foregroundSync.run(this) } }
        }
        setContent {
            // Nothing is drawn until the saved theme is read (a few ms; the window background
            // already matches the phone's mode), so a chosen theme never flashes the other one.
            val mode by themeSettings.mode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = mode?.isDark(systemDark) ?: return@setContent
            // Status/navigation bar icons follow the app theme, not only the phone's mode.
            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            CoShellTheme(darkTheme = darkTheme) {
                CoShellNavHost()
            }
        }
    }
}
