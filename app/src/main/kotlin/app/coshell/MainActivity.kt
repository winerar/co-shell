package app.coshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.family.sync.ForegroundSync
import app.coshell.navigation.CoShellNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var foregroundSync: ForegroundSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Family sync while the app is visible; stops in the background (WorkManager takes over).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { coroutineScope { foregroundSync.run(this) } }
        }
        setContent {
            CoShellTheme {
                CoShellNavHost()
            }
        }
    }
}
