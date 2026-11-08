package app.coshell.di

import android.content.Context
import androidx.room.Room
import app.coshell.core.data.db.AccountDao
import app.coshell.core.data.db.CategoryDao
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.TransactionDao
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.domain.repository.AccountRepository
import app.coshell.core.domain.repository.CategoryRepository
import app.coshell.core.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** Wires the KMP :core:data module (which has no Hilt) into the Android app. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): CoShellDatabase =
        Room.databaseBuilder<CoShellDatabase>(context, context.getDatabasePath(CoShellDatabase.NAME).path)
            .build()

    @Provides fun accountDao(db: CoShellDatabase): AccountDao = db.accountDao()
    @Provides fun categoryDao(db: CoShellDatabase): CategoryDao = db.categoryDao()
    @Provides fun transactionDao(db: CoShellDatabase): TransactionDao = db.transactionDao()

    @Provides fun clock(): Clock = Clock.System

    @Provides
    @Singleton
    fun accounts(dao: AccountDao, clock: Clock): AccountRepository = RoomAccountRepository(dao, clock)

    @Provides
    @Singleton
    fun categories(dao: CategoryDao, clock: Clock): CategoryRepository = RoomCategoryRepository(dao, clock)

    @Provides
    @Singleton
    fun transactions(dao: TransactionDao, clock: Clock): TransactionRepository =
        RoomTransactionRepository(dao, clock)
}
