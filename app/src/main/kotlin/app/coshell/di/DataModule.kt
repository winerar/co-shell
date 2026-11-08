package app.coshell.di

import android.content.Context
import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomBudgetRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.BudgetRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.DeleteAccountUseCase
import app.coshell.core.domain.DeleteCategoryUseCase
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.IdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoShellDatabase = createCoShellDatabase(context)

    @Provides @Singleton
    fun provideStamper(): WriteStamper = WriteStamper()

    @Provides
    fun provideIdGenerator(): IdGenerator = IdGenerator.Random

    @Provides @Singleton
    fun provideAccountRepository(db: CoShellDatabase, stamper: WriteStamper): AccountRepository =
        RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)

    @Provides @Singleton
    fun provideCategoryRepository(db: CoShellDatabase, stamper: WriteStamper): CategoryRepository =
        RoomCategoryRepository(db.categoryDao(), stamper)

    @Provides @Singleton
    fun provideTransactionRepository(db: CoShellDatabase, stamper: WriteStamper): TransactionRepository =
        RoomTransactionRepository(db.transactionDao(), stamper)

    @Provides @Singleton
    fun provideBudgetRepository(db: CoShellDatabase, stamper: WriteStamper): BudgetRepository =
        RoomBudgetRepository(db.budgetDao(), stamper)

    @Provides @Singleton
    fun provideBackupRepository(db: CoShellDatabase, stamper: WriteStamper): BackupRepository =
        JsonBackupRepository(db, stamper)

    @Provides
    fun provideSaveTransaction(
        accounts: AccountRepository,
        categories: CategoryRepository,
        transactions: TransactionRepository,
    ) = SaveTransactionUseCase(accounts, categories, transactions)

    @Provides
    fun provideDeleteAccount(accounts: AccountRepository, transactions: TransactionRepository) =
        DeleteAccountUseCase(accounts, transactions)

    @Provides
    fun provideDeleteCategory(categories: CategoryRepository, transactions: TransactionRepository) =
        DeleteCategoryUseCase(categories, transactions)

    @Provides
    fun provideSeedDefaultCategories(categories: CategoryRepository, ids: IdGenerator) =
        SeedDefaultCategoriesUseCase(categories, ids)
}
