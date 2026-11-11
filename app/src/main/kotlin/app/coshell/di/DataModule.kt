package app.coshell.di

import android.content.Context
import app.coshell.core.data.auth.AuthManager
import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomBudgetRepository
import app.coshell.core.data.repository.RoomTemplateRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomExchangeRateRepository
import app.coshell.core.data.repository.RoomImportRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.ArchiveCategoryUseCase
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.BudgetRepository
import app.coshell.core.domain.templates.ApplyTemplateUseCase
import app.coshell.core.domain.templates.SaveAsTemplateUseCase
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.DataWipeRepository
import app.coshell.core.domain.DeleteAccountUseCase
import app.coshell.core.domain.DeleteCategoryUseCase
import app.coshell.core.domain.EnsureAdjustmentCategoriesUseCase
import app.coshell.core.domain.SaveAccountUseCase
import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.WipeAllDataUseCase
import app.coshell.core.domain.importer.ImportRepository
import app.coshell.core.domain.importer.wallet.WalletImportUseCase
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.model.IdGenerator
import app.coshell.importer.wallet.ImportAccountColors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoShellDatabase = createCoShellDatabase(context)

    /** Local edits are stamped with the signed-in member (sync, «who added it»). */
    @Provides @Singleton
    fun provideStamper(auth: AuthManager, clock: Clock): WriteStamper = WriteStamper(clock) { auth.uid }

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
    fun provideTemplateRepository(db: CoShellDatabase, stamper: WriteStamper): TemplateRepository =
        RoomTemplateRepository(db.templateDao(), stamper)

    @Provides
    fun provideSaveTemplate(accounts: AccountRepository, categories: CategoryRepository, templates: TemplateRepository) =
        SaveTemplateUseCase(accounts, categories, templates)

    @Provides
    fun provideSaveAsTemplate(templates: TemplateRepository, save: SaveTemplateUseCase, ids: IdGenerator) =
        SaveAsTemplateUseCase(templates, save, ids)

    @Provides
    fun provideApplyTemplate(accounts: AccountRepository, categories: CategoryRepository, templates: TemplateRepository) =
        ApplyTemplateUseCase(accounts, categories, templates)

    @Provides @Singleton
    fun provideExchangeRateRepository(db: CoShellDatabase, stamper: WriteStamper): ExchangeRateRepository =
        RoomExchangeRateRepository(db.rateDao(), stamper)

    @Provides @Singleton
    fun provideImportRepository(db: CoShellDatabase, stamper: WriteStamper): ImportRepository =
        RoomImportRepository(db.importDao(), stamper)

    @Provides
    fun provideWalletImport(
        accounts: AccountRepository,
        imports: ImportRepository,
        categories: CategoryRepository,
        ids: IdGenerator,
    ): WalletImportUseCase = WalletImportUseCase(accounts, imports, EnsureAdjustmentCategoriesUseCase(categories), ids, ImportAccountColors)

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
    fun provideSaveAccount(accounts: AccountRepository, transactions: TransactionRepository) =
        SaveAccountUseCase(accounts, transactions)

    @Provides
    fun provideSaveCategory(categories: CategoryRepository, transactions: TransactionRepository) =
        SaveCategoryUseCase(categories, transactions)

    @Provides
    fun provideArchiveCategory(categories: CategoryRepository) = ArchiveCategoryUseCase(categories)

    @Provides
    fun provideDeleteAccount(accounts: AccountRepository, transactions: TransactionRepository) =
        DeleteAccountUseCase(accounts, transactions)

    @Provides
    fun provideDeleteCategory(categories: CategoryRepository, transactions: TransactionRepository) =
        DeleteCategoryUseCase(categories, transactions)

    @Provides
    fun provideSeedDefaultCategories(categories: CategoryRepository, ids: IdGenerator) =
        SeedDefaultCategoriesUseCase(categories, ids)

    @Provides
    fun provideDataWipeRepository(db: CoShellDatabase, stamper: WriteStamper): DataWipeRepository =
        RoomDataWipeRepository(db.wipeDao(), stamper)

    @Provides
    fun provideWipeAllData(wipe: DataWipeRepository, seed: SeedDefaultCategoriesUseCase) = WipeAllDataUseCase(wipe, seed)
}
