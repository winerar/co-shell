package app.coshell.di

import app.coshell.BuildConfig
import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.ReleaseSource
import app.coshell.core.domain.update.UpdateStateStore
import app.coshell.core.domain.update.WhatsNewUseCase
import app.coshell.update.data.ApkDownloader
import app.coshell.update.data.CacheApkDownloader
import app.coshell.update.data.HttpReleaseSource
import app.coshell.update.data.PrefsUpdateStateStore
import app.coshell.update.install.ApkInstaller
import app.coshell.update.install.PackageInstallerApkInstaller
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock

/** Self-update: `version.json` in GitHub Releases of `co-shell-releases`, download, PackageInstaller. */
@Module
@InstallIn(SingletonComponent::class)
interface UpdateModule {
    @Binds fun bindStateStore(impl: PrefsUpdateStateStore): UpdateStateStore
    @Binds fun bindDownloader(impl: CacheApkDownloader): ApkDownloader
    @Binds fun bindInstaller(impl: PackageInstallerApkInstaller): ApkInstaller

    companion object {
        @Provides
        fun provideInstalledVersion() = InstalledVersion(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)

        /** Empty in local builds: CI sets `UPDATE_BASE_URL` (see build-and-test.md §Самообновление). */
        @Provides
        fun provideReleaseSource(): ReleaseSource = HttpReleaseSource(BuildConfig.UPDATE_BASE_URL)

        @Provides
        fun provideCheckForUpdate(
            source: ReleaseSource,
            store: UpdateStateStore,
            installed: InstalledVersion,
            clock: Clock,
        ) = CheckForUpdateUseCase(source, store, installed, clock)

        @Provides
        fun provideWhatsNew(store: UpdateStateStore, installed: InstalledVersion) = WhatsNewUseCase(store, installed)
    }
}
