package app.coshell.di

import app.coshell.backup.ContentResolverDocumentStorage
import app.coshell.backup.DocumentStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {
    @Binds
    fun bindDocumentStorage(impl: ContentResolverDocumentStorage): DocumentStorage
}
