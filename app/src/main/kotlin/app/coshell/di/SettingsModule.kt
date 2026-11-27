package app.coshell.di

import app.coshell.core.domain.quickentry.TipSettings
import app.coshell.core.domain.settings.ConverterSettings
import app.coshell.core.domain.settings.ListDensitySettings
import app.coshell.core.domain.settings.ScreenOptionsStore
import app.coshell.core.domain.settings.SuggestionDismissals
import app.coshell.settings.PrefsSuggestionDismissals
import app.coshell.settings.PrefsScreenOptions
import app.coshell.core.domain.settings.MonthReviewSettings
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.core.domain.shake.ShakeSettings
import app.coshell.core.domain.templates.TemplateUsage
import app.coshell.notices.data.PrefsNoticeSettings
import app.coshell.recurring.notify.data.PrefsReminderSettings
import app.coshell.reports.review.data.PrefsMonthReviewSettings
import app.coshell.settings.PrefsConverterSettings
import app.coshell.settings.PrefsListDensitySettings
import app.coshell.shake.data.PrefsShakeSettings
import app.coshell.templates.data.PrefsTemplateUsage
import app.coshell.theme.data.PrefsThemeSettings
import app.coshell.transactions.edit.tip.PrefsTipSettings
import app.coshell.core.domain.privacy.PinHasher
import app.coshell.core.domain.privacy.PrivacySettings
import app.coshell.privacy.data.Pbkdf2PinHasher
import app.coshell.privacy.data.PrefsPrivacySettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Device-only preferences (not synced, not in the backup). Family settings — `DataModule`. */
@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {
    @Binds
    fun bindPrivacySettings(impl: PrefsPrivacySettings): PrivacySettings

    @Binds
    fun bindPinHasher(impl: Pbkdf2PinHasher): PinHasher

    @Binds
    fun bindShakeSettings(impl: PrefsShakeSettings): ShakeSettings

    @Binds
    fun bindThemeSettings(impl: PrefsThemeSettings): ThemeSettings

    @Binds
    fun bindTemplateUsage(impl: PrefsTemplateUsage): TemplateUsage

    @Binds
    fun bindMonthReviewSettings(impl: PrefsMonthReviewSettings): MonthReviewSettings

    @Binds
    fun bindReminderSettings(impl: PrefsReminderSettings): ReminderSettings

    @Binds
    fun bindNoticeSettings(impl: PrefsNoticeSettings): NoticeSettings

    @Binds
    fun bindTipSettings(impl: PrefsTipSettings): TipSettings

    @Binds
    fun bindScreenOptions(impl: PrefsScreenOptions): ScreenOptionsStore

    @Binds
    fun bindListDensitySettings(impl: PrefsListDensitySettings): ListDensitySettings

    @Binds
    fun bindConverterSettings(impl: PrefsConverterSettings): ConverterSettings

    @Binds
    fun bindSuggestionDismissals(impl: PrefsSuggestionDismissals): SuggestionDismissals
}
