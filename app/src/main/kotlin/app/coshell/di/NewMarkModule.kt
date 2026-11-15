package app.coshell.di

import android.content.Context
import app.coshell.core.domain.newmark.NewMarkStore
import app.coshell.core.domain.newmark.NewMarks
import app.coshell.newmark.PrefsNewMarkStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock

/** The «новое» marks (#335): per-phone state and whether the app was here before this version. */
@Module
@InstallIn(SingletonComponent::class)
interface NewMarkModule {
    @Binds fun bindStore(impl: PrefsNewMarkStore): NewMarkStore

    companion object {
        /** Updated = Android installed the package earlier than its last update; unknown counts as a fresh install. */
        @Provides
        fun provideNewMarks(@ApplicationContext context: Context, store: NewMarkStore, clock: Clock): NewMarks {
            val info = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
            val wasUpdated = info != null && info.firstInstallTime > 0 && info.lastUpdateTime > info.firstInstallTime
            return NewMarks(store, clock, wasUpdated)
        }
    }
}
