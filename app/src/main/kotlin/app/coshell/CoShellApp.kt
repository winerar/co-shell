package app.coshell

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.family.sync.SyncScheduler
import app.coshell.rates.work.RatesRefreshScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CoShellApp : Application(), Configuration.Provider {

    @Inject lateinit var seedDefaultCategories: SeedDefaultCategoriesUseCase
    @Inject lateinit var workerFactory: HiltWorkerFactory

    /** Lives as long as the process; for short one-off startup work only. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** WorkManager is initialized on demand with Hilt workers (default initializer removed in the manifest). */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            seedDefaultCategories()
            RatesRefreshScheduler.schedule(this@CoShellApp)
            SyncScheduler.schedulePeriodic(this@CoShellApp)
        }
    }
}
