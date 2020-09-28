package com.alamkanak.weekview

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun LocalDate.isNotEqual(other: LocalDate) = !isEqual(other)

internal fun LocalDateTime.isNotEqual(other: LocalDateTime) = !isEqual(other)

internal val LocalDateTime.millis: Long
    get() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

internal fun LocalDate.plusDays(days: Int) = plusDays(days.toLong())

internal fun LocalDate.minusDays(days: Int) = minusDays(days.toLong())

internal fun LocalDateTime.minusMinutes(minutes: Int) = minusMinutes(minutes.toLong())

internal val LocalDate.isBeforeToday: Boolean
    get() = isBefore(LocalDate.now())

internal val LocalDate.isToday: Boolean
    get() = isEqual(LocalDate.now())

internal fun LocalDateTime.withTimeAtStartOfPeriod(hour: Int): LocalDateTime {
    return withHour(hour)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
}

internal fun LocalDateTime.withTimeAtEndOfPeriod(hour: Int): LocalDateTime {
    return withHour(hour - 1)
        .withMinute(59)
        .withSecond(59)
        .withNano(999_999)
}

internal val LocalDate.daysFromToday: Int
    get() = ChronoUnit.DAYS.between(LocalDate.now(), this).toInt()

internal typealias DateRange = List<LocalDate>

internal fun DateRange.limitTo(minDate: LocalDate?, maxDate: LocalDate?): List<LocalDate> {
    if (minDate == null && maxDate == null) {
        return this
    }

    val firstDate = firstOrNull() ?: return this
    val lastDate = lastOrNull() ?: return this
    val numberOfDays = size

    val mustAdjustStart = minDate != null && firstDate < minDate
    val mustAdjustEnd = maxDate != null && lastDate > maxDate

    if (mustAdjustStart && mustAdjustEnd) {
        // The date range is longer than the range from min date to max date.
        throw IllegalStateException("Can't render $numberOfDays days " +
            "between the provided minDate and maxDate.")
    }

    return when {
        mustAdjustStart -> {
            minDate!!.rangeWithDays(numberOfDays)
        }
        mustAdjustEnd -> {
            val start = maxDate!!.minusDays(numberOfDays - 1)
            start.rangeWithDays(numberOfDays)
        }
        else -> {
            this
        }
    }
}

internal fun LocalDate.rangeWithDays(days: Int) = (0 until days).map { this.plusDays(it) }

internal val LocalDate.isWeekend: Boolean
    get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

internal fun Calendar.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun Calendar.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

internal val YearMonth.startDate: LocalDate
    get() = atDay(1)

internal val YearMonth.endDate: LocalDate
    get() = atEndOfMonth()

internal val LocalDateTime.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

internal fun LocalDate.toCalendar(): Calendar = atStartOfDay().toCalendar()

internal fun LocalDateTime.toCalendar(): Calendar {
    val instant = atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(instant)
    return calendar
}

/**
 * Checks if this date is at the start of the next day after startTime.
 * For example, if the start date was January the 1st and startDate was January the 2nd at 00:00,
 * this method would return true.
 * @param startDateTime The start date of the event
 * @return Whether or not this date is at the start of the day after startDate
 */
internal fun LocalDateTime.isAtStartOfNextDay(startDateTime: LocalDateTime): Boolean {
    return if (isEqual(this.toLocalDate().atStartOfDay())) {
        val endOfPreviousDay = this.minusNanos(1)
        endOfPreviousDay.toLocalDate() == startDateTime.toLocalDate()
    } else {
        false
    }
}

internal fun defaultDateFormatter(
    numberOfDays: Int
): DateTimeFormatter = when (numberOfDays) {
    1 -> DateTimeFormatter.ofPattern("EEEE M/dd", Locale.getDefault()) // full weekday
    in 2..6 -> DateTimeFormatter.ofPattern("EEE M/dd", Locale.getDefault()) // first three characters
    else -> DateTimeFormatter.ofPattern("EEEEE M/dd", Locale.getDefault()) // first character
}

internal fun defaultTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("hh a", Locale.getDefault())
