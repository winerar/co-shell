package app.coshell.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock
import kotlinx.datetime.TimeZone

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun provideClock(): Clock = Clock.System

    /** Not a singleton: the user may travel and change the phone's time zone. */
    @Provides
    fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()
}
