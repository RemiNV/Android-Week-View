package com.alamkanak.weekview

import androidx.collection.ArrayMap
import java.time.LocalDate
import java.time.YearMonth

/**
 * An abstract class that provides functionality to cache [WeekViewEvent]s.
 */
internal abstract class EventsCache<T> {

    abstract val allEvents: List<ResolvedWeekViewEvent<T>>
    abstract fun update(events: List<ResolvedWeekViewEvent<T>>)
    abstract fun clear()

    operator fun get(id: Long): ResolvedWeekViewEvent<T>? = allEvents.firstOrNull { it.id == id }

    operator fun get(
        dateRange: List<LocalDate>
    ): List<ResolvedWeekViewEvent<T>> {
        val startDate = checkNotNull(dateRange.minOrNull())
        val endDate = checkNotNull(dateRange.maxOrNull())
        return allEvents.filter { it.endTime.toLocalDate() >= startDate || it.startTime.toLocalDate() <= endDate }
    }

    operator fun get(
        fetchRange: FetchRange
    ): List<ResolvedWeekViewEvent<T>> {
        val startTime = fetchRange.previous.startDate
        val endTime = fetchRange.next.endDate
        return allEvents.filter { it.endTime.toLocalDate() >= startTime && it.startTime.toLocalDate() <= endTime }
    }
}

/**
 * Represents an [EventsCache] that relies on a simple list of [WeekViewEvent]s. When updated with
 * new [WeekViewEvent]s, all existing ones are replaced.
 */
internal class SimpleEventsCache<T> : EventsCache<T>() {

    private var _allEvents: List<ResolvedWeekViewEvent<T>>? = null

    override val allEvents: List<ResolvedWeekViewEvent<T>>
        get() = _allEvents.orEmpty()

    override fun update(events: List<ResolvedWeekViewEvent<T>>) {
        _allEvents = events
    }

    override fun clear() {
        _allEvents = null
    }
}

/**
 * Represents an [EventsCache] that caches [ResolvedWeekViewEvent]s for their respective [YearMonth]
 * and allows retrieval based on that [YearMonth].
 */
internal class PagedEventsCache<T> : EventsCache<T>() {

    override val allEvents: List<ResolvedWeekViewEvent<T>>
        get() = eventsByPeriod.values.flatten()

    private val eventsByPeriod: ArrayMap<YearMonth, List<ResolvedWeekViewEvent<T>>> = ArrayMap()

    override fun update(events: List<ResolvedWeekViewEvent<T>>) {
        val groupedEvents = events.groupBy { it.startTime.yearMonth }
        for ((period, periodEvents) in groupedEvents) {
            eventsByPeriod[period] = periodEvents
        }
    }

    internal fun determinePeriodsToFetch(range: FetchRange) = range.periods.filter { it !in this }

    operator fun contains(month: YearMonth) = eventsByPeriod.contains(month)

    operator fun contains(range: FetchRange) = eventsByPeriod.containsAll(range.periods)

    fun reserve(month: YearMonth) {
        eventsByPeriod[month] = listOf()
    }

    override fun clear() {
        eventsByPeriod.clear()
    }
}
