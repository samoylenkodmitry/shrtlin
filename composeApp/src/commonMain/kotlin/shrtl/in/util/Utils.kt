package shrtl.`in`.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun timestampToDate(date: Long): String {
    val instant = Instant.fromEpochMilliseconds(date)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.time}"
}
