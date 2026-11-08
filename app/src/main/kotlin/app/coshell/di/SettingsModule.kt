package app.coshell.di

import app.coshell.core.domain.shake.ShakeSettings
import app.coshell.shake.data.PrefsShakeSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Device-only preferences (not synced, not in the backup). */
@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {
    @Binds
    fun bindShakeSettings(impl: PrefsShakeSettings): ShakeSettings
}
