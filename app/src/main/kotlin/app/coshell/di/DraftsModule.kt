package app.coshell.di

import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.repository.RoomDraftRepository
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.domain.drafts.PostDraftsUseCase
import app.coshell.core.domain.drafts.ProcessQuickNoteUseCase
import app.coshell.core.model.IdGenerator
import app.coshell.drafts.LocalCurrency
import app.coshell.drafts.NoteStore
import app.coshell.drafts.PrefsNoteStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** Quick notes and drafts (#586): drafts live only on this phone, the model (if installed) reads the lines rules cannot. */
@Module
@InstallIn(SingletonComponent::class)
object DraftsModule {
    @Provides @Singleton
    fun provideDraftRepository(db: CoShellDatabase): DraftRepository = RoomDraftRepository(db.draftDao())

    @Provides
    fun provideNoteStore(impl: PrefsNoteStore): NoteStore = impl

    @Provides
    fun provideProcessQuickNote(
        accounts: AccountRepository,
        categories: CategoryRepository,
        transactions: TransactionRepository,
        drafts: DraftRepository,
        ids: IdGenerator,
        clock: Clock,
        local: LocalCurrency,
    ) = ProcessQuickNoteUseCase(accounts, categories, transactions, drafts, ids, local::current) { clock.now().toEpochMilliseconds() }

    @Provides
    fun provideEntryDraftStore(impl: app.coshell.transactions.edit.PrefsEntryDraftStore): app.coshell.core.domain.quickentry.EntryDraftStore = impl

    @Provides
    fun providePostDrafts(
        accounts: AccountRepository,
        categories: CategoryRepository,
        drafts: DraftRepository,
        save: SaveTransactionUseCase,
        ids: IdGenerator,
    ) = PostDraftsUseCase(accounts, categories, drafts, save, ids)
}
