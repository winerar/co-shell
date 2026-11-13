package app.coshell

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import app.coshell.core.data.family.FamilyService
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.tags.EnsureLabelTagsUseCase
import app.coshell.crash.CrashLog
import app.coshell.crash.CrashRecorder
import app.coshell.currencies.data.LegacyReportCurrencies
import app.coshell.family.sync.SyncScheduler
import app.coshell.language.AppLanguage
import app.coshell.rates.work.RatesRefreshScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CoShellApp : Application(), Configuration.Provider {

    @Inject lateinit var seedDefaultCategories: SeedDefaultCategoriesUseCase
    @Inject lateinit var family: FamilyService
    @Inject lateinit var ensureLabelTags: EnsureLabelTagsUseCase
    @Inject lateinit var legacyReportCurrencies: LegacyReportCurrencies
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var crashLog: CrashLog

    /** Lives as long as the process; for short one-off startup work only. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** WorkManager is initialized on demand with Hilt workers (default initializer removed in the manifest). */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    // Notifications and workers speak the app's language too, not the phone's (AppLanguage).
    override fun attachBaseContext(base: Context) = super.attachBaseContext(AppLanguage.wrap(base))

    override fun onCreate() {
        super.onCreate()
        CrashRecorder(crashLog, Clock.System, BuildConfig.VERSION_CODE).install()
        appScope.launch {
            legacyReportCurrencies.moveToFamilySettings()
            // In a family the categories come from the server: seeding an empty phone right after
            // joining (before the first sync) would send duplicate default categories to everyone.
            family.load()
            if (family.joined == null) seedDefaultCategories()
            // Wallet labels become tags (#129); new ones after an import are picked up on the next start.
            ensureLabelTags()
            RatesRefreshScheduler.schedule(this@CoShellApp)
            SyncScheduler.schedulePeriodic(this@CoShellApp)
        }
    }
}
