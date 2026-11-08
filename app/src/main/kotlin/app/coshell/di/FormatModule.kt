package app.coshell.di

import app.coshell.core.model.MoneyFormat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FormatModule {
    // TODO(stage 1): choose the preset from the user's locale / settings.
    @Provides
    fun provideMoneyFormat(): MoneyFormat = MoneyFormat.RU
}
