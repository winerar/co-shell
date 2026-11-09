package app.coshell.di

import app.coshell.core.data.rates.defaultRatesProvider
import app.coshell.core.domain.rates.RatesProvider
import app.coshell.rates.data.UrlConnectionHttpGet
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Exchange rates download (ARCHITECTURE §6): currency-api → its mirror → open.er-api. */
@Module
@InstallIn(SingletonComponent::class)
object RatesModule {
    @Provides @Singleton
    fun provideRatesProvider(): RatesProvider = defaultRatesProvider(UrlConnectionHttpGet())
}
