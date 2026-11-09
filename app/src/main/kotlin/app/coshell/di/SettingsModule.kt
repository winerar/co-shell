package app.coshell.di

import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.core.domain.shake.ShakeSettings
import app.coshell.currencies.data.PrefsReportCurrencySettings
import app.coshell.shake.data.PrefsShakeSettings
import app.coshell.theme.data.PrefsThemeSettings
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

    @Binds
    fun bindReportCurrencySettings(impl: PrefsReportCurrencySettings): ReportCurrencySettings

    @Binds
    fun bindThemeSettings(impl: PrefsThemeSettings): ThemeSettings
}
