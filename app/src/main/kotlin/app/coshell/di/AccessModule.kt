package app.coshell.di

import android.content.Context
import app.coshell.BuildConfig
import app.coshell.access.AccessCenter
import app.coshell.access.AccessEnabled
import app.coshell.access.PrefsAccessStore
import app.coshell.core.data.remote.FirestoreAccessRemote
import app.coshell.core.data.remote.HttpClient
import app.coshell.core.domain.access.AccessGate
import app.coshell.core.domain.access.AccessRemote
import app.coshell.core.domain.access.AccessStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** The invitation gate of the closed test (#666). Off unless the build asks for it and has the Firebase keys to check codes with. */
@Module
@InstallIn(SingletonComponent::class)
object AccessModule {
    @Provides @Singleton
    fun provideAccessStore(@ApplicationContext context: Context): AccessStore = PrefsAccessStore(context)

    @Provides @Singleton
    fun provideAccessRemote(http: HttpClient): AccessRemote =
        FirestoreAccessRemote(http, BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_API_KEY)

    @Provides @Singleton
    fun provideAccessGate(remote: AccessRemote, store: AccessStore, clock: Clock): AccessGate =
        AccessGate(remote, store) { clock.now().toEpochMilliseconds() }

    @Provides @Singleton
    fun provideAccessCenter(gate: AccessGate, store: AccessStore): AccessCenter {
        val keys = BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() && BuildConfig.FIREBASE_API_KEY.isNotBlank()
        return AccessCenter(gate, store, AccessEnabled(BuildConfig.ACCESS_GATE && keys))
    }
}
