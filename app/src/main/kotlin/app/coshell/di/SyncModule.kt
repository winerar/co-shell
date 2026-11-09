package app.coshell.di

import android.content.Context
import app.coshell.BuildConfig
import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.auth.AuthManager
import app.coshell.core.data.auth.FirebaseAuthApi
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.remote.FirestoreClient
import app.coshell.core.data.remote.FirestoreFamilyRemote
import app.coshell.core.data.remote.FirestoreRemoteStore
import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.family.data.PrefsSessionStore
import app.coshell.family.data.SafetyBackupWriter
import app.coshell.family.data.UrlConnectionHttpClient
import app.coshell.family.sync.ForegroundSync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** Family sync over Firestore REST (docs/ARCHITECTURE.md §10, setup — docs/setup/sync.md). */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    /** All three values come from CI variables; local builds have none and work offline only. */
    val isConfigured: Boolean
        get() = BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() && BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    @Provides @Singleton
    fun provideHttpClient(): HttpClient = UrlConnectionHttpClient()

    @Provides @Singleton
    fun provideAuth(@ApplicationContext context: Context, http: HttpClient, clock: Clock): AuthManager =
        AuthManager(FirebaseAuthApi(http, BuildConfig.FIREBASE_API_KEY), PrefsSessionStore(context), clock)

    @Provides
    fun provideAuthGateway(auth: AuthManager): AuthGateway = auth

    @Provides @Singleton
    fun provideFirestore(http: HttpClient, auth: AuthManager): FirestoreClient =
        FirestoreClient(http, BuildConfig.FIREBASE_PROJECT_ID, auth)

    @Provides
    fun provideFamilyRemote(client: FirestoreClient): FamilyRemote = FirestoreFamilyRemote(client)

    @Provides
    fun provideSyncDao(db: CoShellDatabase): SyncDao = db.syncDao()

    @Provides @Singleton
    fun provideSyncEngine(dao: SyncDao, client: FirestoreClient, clock: Clock): SyncEngine =
        SyncEngine(dao, FirestoreRemoteStore(client, FamilyRemote.FAMILY_ID), clock)

    @Provides @Singleton
    fun provideFamilyService(
        @ApplicationContext context: Context,
        auth: AuthManager,
        remote: FamilyRemote,
        dao: SyncDao,
        engine: SyncEngine,
        backup: BackupRepository,
    ): FamilyService {
        val writer = SafetyBackupWriter(context)
        return FamilyService(isConfigured, auth, remote, dao, engine, backup) { writer.write(it) }
    }

    @Provides
    fun provideForegroundSync(@ApplicationContext context: Context, family: FamilyService, dao: SyncDao) =
        ForegroundSync(context, family, dao)
}
