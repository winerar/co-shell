package app.coshell.di

import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.budgets.RemoveBudgetUseCase
import app.coshell.core.domain.budgets.SaveBudgetUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Budget use cases (#103); the repository itself is in [DataModule]. */
@Module
@InstallIn(SingletonComponent::class)
object BudgetModule {
    @Provides
    fun provideSaveBudget(categories: CategoryRepository, budgets: BudgetRepository) = SaveBudgetUseCase(categories, budgets)

    @Provides
    fun provideRemoveBudget(budgets: BudgetRepository) = RemoveBudgetUseCase(budgets)
}
