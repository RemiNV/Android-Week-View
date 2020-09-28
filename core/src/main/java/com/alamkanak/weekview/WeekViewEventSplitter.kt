package com.alamkanak.weekview

import java.time.temporal.ChronoUnit

internal fun ResolvedWeekViewEvent<*>.split(viewState: ViewState): List<ResolvedWeekViewEvent<*>> {
    if (startTime >= endTime) {
        return emptyList()
    }

    // Check whether the end date of the event is exactly 12 AM. If so, the event will be
    // shortened by a millisecond.
    val endsOnStartOfNextDay = endTime.isAtStartOfNextDay(startTime)
    val isAtStartOfNextPeriod = viewState.minHour == 0 && endsOnStartOfNextDay

    return when {
        isAtStartOfNextPeriod -> listOf(shortenTooLongAllDayEvent(viewState))
        isMultiDay -> splitEventByDates(viewState)
        else -> listOf(this)
    }
}

private fun ResolvedWeekViewEvent<*>.shortenTooLongAllDayEvent(
    viewState: ViewState
): ResolvedWeekViewEvent<*> {
    val newEndTime = endTime.withTimeAtEndOfPeriod(viewState.maxHour)
    return copy(endTime = newEndTime)
}

private fun ResolvedWeekViewEvent<*>.splitEventByDates(
    viewState: ViewState
): List<ResolvedWeekViewEvent<*>> {
    val results = mutableListOf<ResolvedWeekViewEvent<*>>()

    val firstEventEnd = startTime.withTimeAtEndOfPeriod(viewState.maxHour)
    val firstEvent = copy(endTime = firstEventEnd)
    results += firstEvent

    val lastEventStart = endTime.withTimeAtStartOfPeriod(viewState.minHour)
    val lastEvent = copy(startTime = lastEventStart)
    results += lastEvent

    val daysInBetween = ChronoUnit.DAYS.between(firstEvent.startTime.toLocalDate(), lastEvent.startTime.toLocalDate())

    if (daysInBetween > 0) {
        var start = firstEventEnd.withTimeAtStartOfPeriod(viewState.minHour).plusDays(1)
        while (start.toLocalDate().isNotEqual(lastEventStart.toLocalDate())) {
            val intermediateStart = start.withTimeAtStartOfPeriod(viewState.minHour)
            val intermediateEnd = start.withTimeAtEndOfPeriod(viewState.maxHour)
            results += copy(startTime = intermediateStart, endTime = intermediateEnd)
            start = start.plusDays(1)
        }
    }

    return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
}
