package app.coshell.core.domain.rates

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** The newest downloaded rates: the date they are published for, the source, and when they arrived. */
data class DownloadInfo(val date: LocalDate, val source: String, val downloadedAt: Instant)
