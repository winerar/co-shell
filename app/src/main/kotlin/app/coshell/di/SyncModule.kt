package app.coshell.di

import android.content.Context
import app.coshell.BuildConfig
import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.auth.AuthManager
import app.coshell.core.data.auth.FirebaseAuthApi
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.integrity.IntegrityCheck
import app.coshell.core.data.remote.FirestoreClient
import app.coshell.core.data.remote.FirestoreFamilyRemote
import app.coshell.core.data.remote.FirestoreRemoteStore
import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.sync.FamilyBell
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.domain.family.OtherMembers
import app.coshell.family.data.PrefsSessionStore
import app.coshell.family.data.SafetyBackupWriter
import app.coshell.family.data.UrlConnectionEventStream
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

    /**
     * Realtime Database for the sync bell: the `FIREBASE_DATABASE_URL` variable, or the default
     * database of the project in us-central1. A database that is not created yet only makes the
     * bell retry quietly; polling keeps syncing.
     */
    val databaseUrl: String
        get() = BuildConfig.FIREBASE_DATABASE_URL.ifBlank {
            BuildConfig.FIREBASE_PROJECT_ID.takeIf { it.isNotBlank() }?.let { "https://$it-default-rtdb.firebaseio.com" }.orEmpty()
        }

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
    fun provideSyncEngine(dao: SyncDao, client: FirestoreClient, clock: Clock, integrity: IntegrityCheck): SyncEngine =
        SyncEngine(dao, FirestoreRemoteStore(client, FamilyRemote.FAMILY_ID), clock, integrity)

    @Provides
    fun provideIntegrityCheck(db: CoShellDatabase): IntegrityCheck = IntegrityCheck(db.integrityDao())

    @Provides @Singleton
    fun provideFamilyService(
        @ApplicationContext context: Context,
        auth: AuthManager,
        remote: FamilyRemote,
        dao: SyncDao,
        engine: SyncEngine,
        backup: BackupRepository,
        bell: FamilyBell,
        clock: Clock,
    ): FamilyService {
        val writer = SafetyBackupWriter(context)
        return FamilyService(isConfigured, auth, remote, dao, engine, backup, clock, bell) { writer.write(it) }
    }

    @Provides @Singleton
    fun provideFamilyBell(http: HttpClient, auth: AuthManager): FamilyBell =
        FamilyBell(http, UrlConnectionEventStream(), if (isConfigured) databaseUrl else "", auth)

    @Provides
    fun provideOtherMembers(family: FamilyService): OtherMembers = OtherMembers { family.otherMemberNames() }

    @Provides
    fun provideForegroundSync(@ApplicationContext context: Context, family: FamilyService, dao: SyncDao, bell: FamilyBell) =
        ForegroundSync(context, family, dao, bell)
}
