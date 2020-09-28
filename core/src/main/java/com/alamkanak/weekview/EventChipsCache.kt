package com.alamkanak.weekview

import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class EventChipsCache {

    val allEventChips: List<EventChip>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>()
    private val allDayEventChipsByDate = ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>()

    fun allEventChipsInDateRange(
        dateRange: List<LocalDate>
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.toEpochDay()].orEmpty()
            results += normalEventChipsByDate[date.toEpochDay()].orEmpty()
        }
        return results
    }

    fun normalEventChipsByDate(
        date: LocalDate
    ): List<EventChip> = normalEventChipsByDate[date.toEpochDay()].orEmpty()

    fun allDayEventChipsByDate(
        date: LocalDate
    ): List<EventChip> = allDayEventChipsByDate[date.toEpochDay()].orEmpty()

    fun allDayEventChipsInDateRange(
        dateRange: List<LocalDate>
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.toEpochDay()].orEmpty()
        }
        return results
    }

    operator fun plusAssign(newChips: List<EventChip>) {
        for (eventChip in newChips) {
            val key = eventChip.event.startTime.toLocalDate().toEpochDay()
            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    fun findHitEvent(x: Float, y: Float): EventChip? {
        val candidates = allEventChips.filter { it.isHit(x, y) }
        return when {
            candidates.isEmpty() -> null
            // Two events hit. This is most likely because an all-day event was clicked, but a
            // single event is rendered underneath it. We return the all-day event.
            candidates.size == 2 -> candidates.first { it.event.isAllDay }
            else -> candidates.first()
        }
    }

    fun clearSingleEventsCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip::setEmpty)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }

    private fun ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>.addOrReplace(
        key: Long,
        eventChip: EventChip
    ) {
        val results = getOrElse(key) { CopyOnWriteArrayList() }
        val indexOfExisting = results.indexOfFirst { it.event.id == eventChip.event.id }
        if (indexOfExisting != -1) {
            // If an event with the same ID already exists, replace it. The new event will likely be
            // more up-to-date.
            results.removeAt(indexOfExisting)
            results.add(indexOfExisting, eventChip)
        } else {
            results.add(eventChip)
        }
        this[key] = results
    }
}
