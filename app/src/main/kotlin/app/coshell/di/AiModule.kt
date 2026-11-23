package app.coshell.di

import android.content.Context
import app.coshell.BuildConfig
import app.coshell.ai.AddonControl
import app.coshell.ai.AddonLanguageModel
import app.coshell.ai.AddonPackage
import app.coshell.ai.AiAddonController
import app.coshell.ai.AiDownloadService
import app.coshell.ai.BoundAddonControl
import app.coshell.ai.DeviceMemory
import app.coshell.ai.SystemDeviceMemory
import app.coshell.ai.cloud.CloudLanguageModel
import app.coshell.ai.cloud.PrefsCloudAiSettings
import app.coshell.core.data.auth.AuthGateway
import app.coshell.family.data.UrlConnectionHttpClient
import app.coshell.core.domain.ai.CloudAiSettings
import app.coshell.core.domain.ai.PayeeCategoryAi
import app.coshell.core.domain.ai.RoutedLanguageModel
import app.coshell.ai.ReleaseAddonPackage
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.update.data.CacheApkDownloader
import app.coshell.update.data.HttpReleaseSource
import app.coshell.update.install.ApkInstaller
import app.coshell.update.install.InstallEvents
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** The optional «ИИ на телефоне» add-on (#587): its control, its APK, the controller and the [LanguageModel] the app asks. */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    @Singleton
    fun provideAddonControl(@ApplicationContext context: Context): AddonControl = BoundAddonControl(context)

    @Provides
    @Singleton
    fun provideAddonPackage(@ApplicationContext context: Context, installer: ApkInstaller): AddonPackage = ReleaseAddonPackage(
        HttpReleaseSource(ReleaseAddonPackage.baseUrl(BuildConfig.UPDATE_BASE_URL)),
        CacheApkDownloader(context, "ai-addon"),
        installer,
    )

    /** One per process: the model download keeps going after the screen is closed. */
    @Provides
    @Singleton
    fun provideController(@ApplicationContext context: Context, control: AddonControl, addon: AddonPackage, events: InstallEvents) =
        AiAddonController(
            control, addon, events.outcomes, CoroutineScope(SupervisorJob() + Dispatchers.Default),
            keepAlive = { AiDownloadService.start(context) },
        )

    @Provides
    fun provideDeviceMemory(@ApplicationContext context: Context): DeviceMemory = SystemDeviceMemory(context)

    @Provides
    @Singleton
    fun provideAddonLanguageModel(@ApplicationContext context: Context) = AddonLanguageModel(context)

    @Provides
    fun provideCloudAiSettings(impl: PrefsCloudAiSettings): CloudAiSettings = impl

    /** Cloud (#635, only when the person allowed it) → the phone → rules. `AI_PROXY_URL` is empty in builds without the Worker. */
    @Provides
    fun providePayeeCategoryAi(model: LanguageModel): PayeeCategoryAi = PayeeCategoryAi(model)

    @Provides
    @Named("aiProxyUrl")
    fun provideAiProxyUrl(): String = BuildConfig.AI_PROXY_URL

    @Provides
    @Singleton
    fun provideLanguageModel(
        addon: AddonLanguageModel,
        auth: AuthGateway,
        settings: PrefsCloudAiSettings,
        clock: Clock,
        @Named("aiProxyUrl") proxyUrl: String,
    ): LanguageModel = RoutedLanguageModel(
        cloud = CloudLanguageModel(
            UrlConnectionHttpClient(timeoutMs = 40_000), auth, proxyUrl, { auth.session.value != null },
            onQuota = { left, limit -> settings.recordQuota(left, limit, clock.now().toEpochMilliseconds() / 86_400_000L) },
        ),
        phone = addon,
        settings = settings,
    )
}
