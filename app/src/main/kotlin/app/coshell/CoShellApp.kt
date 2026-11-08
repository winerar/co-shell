package app.coshell

import android.app.Application
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CoShellApp : Application() {

    @Inject lateinit var seedDefaultCategories: SeedDefaultCategoriesUseCase

    /** Lives as long as the process; for short one-off startup work only. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedDefaultCategories() }
    }
}
