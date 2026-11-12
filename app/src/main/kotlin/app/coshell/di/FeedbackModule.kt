package app.coshell.di

import app.coshell.BuildConfig
import app.coshell.core.domain.feedback.DeviceInfoProvider
import app.coshell.core.domain.feedback.DiagnosticsProvider
import app.coshell.core.domain.feedback.FeedbackSender
import app.coshell.core.domain.feedback.FeedbackStatusSource
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.crash.CrashLog
import app.coshell.crash.FileCrashLog
import app.coshell.feedback.InstalledBuild
import app.coshell.feedback.data.AndroidDeviceInfoProvider
import app.coshell.feedback.data.AndroidDiagnosticsProvider
import app.coshell.feedback.data.FeedbackHistory
import app.coshell.feedback.data.FirestoreFeedbackSender
import app.coshell.feedback.data.FirestoreFeedbackStatusSource
import app.coshell.feedback.data.PrefsFeedbackHistory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock

/** Feedback form → Firestore `feedback` → GitHub issue (docs/setup/feedback.md). */
@Module
@InstallIn(SingletonComponent::class)
interface FeedbackModule {
    @Binds fun bindDeviceInfo(impl: AndroidDeviceInfoProvider): DeviceInfoProvider

    @Binds fun bindDiagnostics(impl: AndroidDiagnosticsProvider): DiagnosticsProvider

    /** The last crash (#218): asked about after the restart, sent in the technical data. */
    @Binds fun bindCrashLog(impl: FileCrashLog): CrashLog

    @Binds fun bindHistory(impl: PrefsFeedbackHistory): FeedbackHistory

    companion object {
        /** Empty in local builds: CI sets `FIREBASE_PROJECT_ID` and `FIREBASE_API_KEY`. */
        @Provides
        fun provideSender(): FeedbackSender =
            FirestoreFeedbackSender(BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_API_KEY)

        @Provides
        fun provideInstalledBuild() = InstalledBuild(BuildConfig.VERSION_CODE)

        @Provides
        fun provideStatusSource(): FeedbackStatusSource =
            FirestoreFeedbackStatusSource(BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_API_KEY)

        @Provides
        fun provideSendFeedback(
            sender: FeedbackSender,
            deviceInfo: DeviceInfoProvider,
            clock: Clock,
            diagnostics: DiagnosticsProvider,
        ) = SendFeedbackUseCase(sender, deviceInfo, clock, diagnostics)
    }
}
