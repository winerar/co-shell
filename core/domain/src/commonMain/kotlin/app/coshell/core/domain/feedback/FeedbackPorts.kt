package app.coshell.core.domain.feedback

import kotlin.time.Instant

/** Service details the app adds to every message; the user writes only the text. */
data class DeviceInfo(
    val appVersion: String,
    val versionCode: Int,
    /** Manufacturer and model, e.g. `Samsung SM-A546E`. */
    val device: String,
    /** OS name and version, e.g. `Android 15 (API 35)`. */
    val os: String,
    /** Random id of this installation: tells apart messages from the two phones. */
    val installationId: String,
)

/** One message from the feedback form as it is sent. */
data class FeedbackReport(val text: String, val createdAt: Instant, val device: DeviceInfo)

interface DeviceInfoProvider {
    suspend fun deviceInfo(): DeviceInfo
}

/** Delivers a report to the developers (Firestore `feedback` → GitHub issue, see ROADMAP). */
interface FeedbackSender {
    /** False when this build has no destination configured (local builds). */
    val isConfigured: Boolean

    /** Returns the id of the stored message (empty if the destination gives none); throws on network or server errors. */
    suspend fun send(report: FeedbackReport): String
}

/** Reads back what happened to a sent message (the robot writes it into the same Firestore document). */
interface FeedbackStatusSource {
    /** `null` — the message is unknown or the status can't be read now (offline, quota). */
    suspend fun status(documentId: String): FeedbackStatus?
}
