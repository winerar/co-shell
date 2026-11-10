package app.coshell.di

import app.coshell.backup.ContentResolverDocumentStorage
import app.coshell.backup.DocumentStorage
import app.coshell.importer.PreImportBackup
import app.coshell.importer.data.FilesPreImportBackup
import app.coshell.wipe.WipeBackup
import app.coshell.wipe.data.DownloadsWipeBackup
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {
    @Binds
    fun bindDocumentStorage(impl: ContentResolverDocumentStorage): DocumentStorage

    @Binds
    fun bindPreImportBackup(impl: FilesPreImportBackup): PreImportBackup

    @Binds
    fun bindWipeBackup(impl: DownloadsWipeBackup): WipeBackup
}
