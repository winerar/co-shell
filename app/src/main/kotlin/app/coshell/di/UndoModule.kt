package app.coshell.di

import app.coshell.undo.SavedEntries
import app.coshell.undo.SavedEntryUndo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The entry sheet reports new saves through [SavedEntries]; the app's one [SavedEntryUndo] listens (#325). */
@Module
@InstallIn(SingletonComponent::class)
abstract class UndoModule {
    @Binds
    abstract fun bindSavedEntries(impl: SavedEntryUndo): SavedEntries
}
