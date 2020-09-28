package com.alamkanak.weekview

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max

internal class WeekViewTouchHandler(
    private val viewState: ViewState
) {

    var adapter: WeekView.Adapter<*>? = null

    fun handleClick(x: Float, y: Float) {
        val inCalendarArea = x > viewState.timeColumnWidth
        if (!inCalendarArea) {
            return
        }

        val handled = adapter?.handleClick(x, y) ?: false
        if (!handled && y > viewState.headerHeight) {
            val time = calculateTimeFromPoint(x, y) ?: return
            adapter?.handleEmptyViewClick(time.toCalendar())
        }
    }

    fun handleLongClick(x: Float, y: Float) {
        val inCalendarArea = x > viewState.timeColumnWidth
        if (!inCalendarArea) {
            return
        }

        val handled = adapter?.handleLongClick(x, y) ?: false
        if (!handled && y > viewState.headerHeight) {
            adapter?.handleLongClick(x, y)
        }
    }

    /**
     * Returns the date and time that the user clicked on.
     *
     * @param touchX The x coordinate of the touch event.
     * @param touchY The y coordinate of the touch event.
     * @return The [LocalDateTime] of the clicked position, or null if none was found.
     */
    internal fun calculateTimeFromPoint(
        touchX: Float,
        touchY: Float
    ): LocalDateTime? {
        val totalDayWidth = viewState.dayWidth
        val originX = viewState.currentOrigin.x
        val timeColumnWidth = viewState.timeColumnWidth

        val daysFromOrigin = (ceil((originX / totalDayWidth).toDouble()) * -1).toInt()
        var startPixel = originX + daysFromOrigin * totalDayWidth + timeColumnWidth

        val firstDay = daysFromOrigin + 1
        val lastDay = firstDay + viewState.numberOfVisibleDays

        for (dayNumber in firstDay..lastDay) {
            val start = max(startPixel, timeColumnWidth)
            val end = startPixel + totalDayWidth
            val width = end - start

            val isVisibleHorizontally = width > 0
            val isWithinDay = touchX in start..end

            if (isVisibleHorizontally && isWithinDay) {
                val day = LocalDate.now().plusDays(dayNumber - 1)

                val hourHeight = viewState.hourHeight
                val pixelsFromMidnight = touchY - viewState.currentOrigin.y - viewState.headerHeight
                val hour = (pixelsFromMidnight / hourHeight).toInt()

                val pixelsFromFullHour = pixelsFromMidnight - hour * hourHeight
                val minutes = ((pixelsFromFullHour / hourHeight) * 60).toInt()

                return day
                    .atStartOfDay()
                    .withHour(viewState.minHour + hour)
                    .withMinute(minutes)
                    .truncatedTo(ChronoUnit.MINUTES)
            }

            startPixel += totalDayWidth
        }

        return null
    }
}
