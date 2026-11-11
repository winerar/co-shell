package app.coshell.feedback.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant as KInstant

private val format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
private val dayFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** «9 нояб. 2026 г.» — the list row, where the time would wrap. */
internal fun feedbackDay(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dayFormat)

/** «9 нояб. 2026 г., 18:21» in the phone's zone and language. */
internal fun feedbackDate(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(format)

internal fun feedbackDate(instant: KInstant): String = feedbackDate(instant.toEpochMilliseconds())
