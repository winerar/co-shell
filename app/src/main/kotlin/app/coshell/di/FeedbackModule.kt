package app.coshell.di

import app.coshell.BuildConfig
import app.coshell.core.domain.feedback.DeviceInfoProvider
import app.coshell.core.domain.feedback.FeedbackSender
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.data.AndroidDeviceInfoProvider
import app.coshell.feedback.data.FeedbackHistory
import app.coshell.feedback.data.PrefsFeedbackHistory
import app.coshell.feedback.data.FirestoreFeedbackSender
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

    @Binds fun bindHistory(impl: PrefsFeedbackHistory): FeedbackHistory

    companion object {
        /** Empty in local builds: CI sets `FIREBASE_PROJECT_ID` and `FIREBASE_API_KEY`. */
        @Provides
        fun provideSender(): FeedbackSender =
            FirestoreFeedbackSender(BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_API_KEY)

        @Provides
        fun provideSendFeedback(sender: FeedbackSender, deviceInfo: DeviceInfoProvider, clock: Clock) =
            SendFeedbackUseCase(sender, deviceInfo, clock)
    }
}
