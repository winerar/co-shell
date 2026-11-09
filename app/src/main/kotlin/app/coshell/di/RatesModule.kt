package app.coshell.di

import app.coshell.core.data.rates.RATES_HISTORY_START
import app.coshell.core.data.rates.defaultRatesProvider
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RatesProvider
import app.coshell.core.domain.rates.RefreshRatesUseCase
import app.coshell.rates.RatesRefresher
import app.coshell.rates.data.UrlConnectionHttpGet
import app.coshell.rates.work.WorkManagerRatesRefresher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** Exchange rates download (ARCHITECTURE §6): currency-api → its mirror → open.er-api. */
@Module
@InstallIn(SingletonComponent::class)
object RatesModule {
    @Provides @Singleton
    fun provideRatesProvider(): RatesProvider = defaultRatesProvider(UrlConnectionHttpGet())

    @Provides
    fun provideRefreshRates(provider: RatesProvider, repository: ExchangeRateRepository, clock: Clock) =
        RefreshRatesUseCase(provider, repository, clock, historyStart = RATES_HISTORY_START)

    /** "Update now" on the rates screen: a one-off run of the same worker. */
    @Provides
    fun provideRatesRefresher(impl: WorkManagerRatesRefresher): RatesRefresher = impl
}
