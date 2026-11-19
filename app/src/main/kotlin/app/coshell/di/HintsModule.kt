package app.coshell.di

import app.coshell.core.domain.hints.HintStore
import app.coshell.hints.PrefsHintStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hints and «Первые шаги» (#513): per-phone state. */
@Module
@InstallIn(SingletonComponent::class)
interface HintsModule {
    @Binds fun bindStore(impl: PrefsHintStore): HintStore
}
