package app.coshell.di

import app.coshell.undo.EditedEntries
import app.coshell.undo.EditedEntryUndo
import app.coshell.undo.SavedEntries
import app.coshell.undo.SavedEntryUndo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The entry sheet reports new saves through [SavedEntries] ([SavedEntryUndo], #325) and edits through [EditedEntries] ([EditedEntryUndo], #798). */
@Module
@InstallIn(SingletonComponent::class)
abstract class UndoModule {
    @Binds
    abstract fun bindSavedEntries(impl: SavedEntryUndo): SavedEntries

    @Binds
    abstract fun bindEditedEntries(impl: EditedEntryUndo): EditedEntries
}
