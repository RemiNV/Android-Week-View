package com.alamkanak.weekview

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

typealias DateFormatter = (Calendar) -> String
typealias TimeFormatter = (Int) -> String

internal class ViewState {

    // View
    var viewWidth: Int = 0
    var viewHeight: Int = 0

    // Calendar state
    var firstVisibleDate: LocalDate = LocalDate.now()
    var scrollToDate: LocalDate? = null
    var scrollToHour: Int? = null

    private var isFirstDraw: Boolean = true

    // Drawing context
    private var startPixel: Float = 0f
    val startPixels: MutableList<Float> = mutableListOf()
    val dateRange: MutableList<LocalDate> = mutableListOf()
    val dateRangeWithStartPixels: MutableList<Pair<LocalDate, Float>> = mutableListOf()

    // Calendar configuration
    var numberOfVisibleDays: Int = 3
    var restoreNumberOfVisibleDays: Boolean = true
    var showFirstDayOfWeekFirst: Boolean = false
    var showCurrentTimeFirst: Boolean = false

    // Time column
    var timeColumnPadding: Int = 0
    var timeColumnHoursInterval: Int = 0

    var headerRowPadding: Float = 0f

    var showWeekNumber: Boolean = false
    var weekNumberBackgroundCornerRadius: Float = 0f

    var eventCornerRadius: Int = 0
    var adaptiveEventTextSize: Boolean = false
    var eventPaddingHorizontal: Int = 0
    var eventPaddingVertical: Int = 0
    var defaultEventColor: Int = 0

    var columnGap: Int = 0
    var overlappingEventGap: Int = 0
    var eventMarginVertical: Int = 0
    var singleDayHorizontalPadding: Int = 0

    var hourHeight: Float = 0f
    var minHourHeight: Float = 0f
    var maxHourHeight: Float = 0f
    var effectiveMinHourHeight: Float = 0f
    var showCompleteDay: Boolean = false

    var showNowLine: Boolean = false
    var showNowLineDot: Boolean = false
    var showHourSeparators: Boolean = false
    var showDaySeparators: Boolean = false
    var showTimeColumnSeparator: Boolean = false
    var showTimeColumnHourSeparators: Boolean = false
    var showHeaderRowBottomLine: Boolean = false
    var showHeaderRowBottomShadow: Boolean = false

    var horizontalScrollingEnabled: Boolean = false

    @Deprecated("No longer used")
    var xScrollingSpeed: Float = 0f
    @Deprecated("No longer used")
    var verticalFlingEnabled: Boolean = false
    @Deprecated("No longer used")
    var horizontalFlingEnabled: Boolean = false
    @Deprecated("No longer used")
    var scrollDuration: Int = 0

    var minHour: Int = 0
    var maxHour: Int = 24

    var typeface: Typeface = Typeface.DEFAULT

    var timeColumnWidth: Float = 0f
    var timeColumnTextHeight: Float = 0f

    val timeColumnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }

    val headerRowTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val headerRowBottomLinePaint = Paint()

    var dateLabelHeight: Float = 0f

    var headerHeight: Float = 0f

    val todayHeaderTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    var currentAllDayEventHeight: Int = 0

    // Dates in the past have origin.x > 0, dates in the future have origin.x < 0
    var currentOrigin = PointF(0f, 0f)

    val headerRowBackgroundPaint = Paint()

    val headerRowBackgroundWithShadowPaint = Paint()

    val dayWidth: Float
        get() = (viewWidth - timeColumnWidth) / numberOfVisibleDays

    val drawableDayWidth: Float
        get() = dayWidth - columnGap

    val dayBackgroundPaint = Paint()

    val hourSeparatorPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val daySeparatorPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    val todayBackgroundPaint = Paint()

    val futureBackgroundPaint = Paint()

    val pastBackgroundPaint = Paint()

    val futureWeekendBackgroundPaint = Paint()

    val pastWeekendBackgroundPaint = Paint()

    val timeColumnSeparatorPaint = Paint()

    val nowLinePaint = Paint()

    val nowDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val eventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val allDayEventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
    }

    val timeColumnBackgroundPaint = Paint()

    var newHourHeight: Float = 0f

    var minDate: LocalDate? = null
    var maxDate: LocalDate? = null

    var dateFormatter: DateFormatter = { date ->
        defaultDateFormatter(numberOfDays = numberOfVisibleDays).format(date.toLocalDate())
    }

    var timeFormatter: TimeFormatter = { hour ->
        val date = LocalDateTime.now().withHour(hour).withMinute(0)
        defaultTimeFormatter().format(date)
    }

    val minX: Float
        get() {
            return maxDate?.let {
                val date = it.minusDays(numberOfVisibleDays - 1)
                getXOriginForDate(date)
            } ?: Float.NEGATIVE_INFINITY
        }

    val maxX: Float
        get() = minDate?.let { getXOriginForDate(it) } ?: Float.POSITIVE_INFINITY

    val isSingleDay: Boolean
        get() = numberOfVisibleDays == 1

    val dayHeight: Float
        get() = (hourHeight * hoursPerDay) + headerHeight

    private val _headerBounds: RectF = RectF()

    val headerBounds: RectF
        get() = _headerBounds.apply {
            left = timeColumnWidth
            top = 0f
            right = viewWidth.toFloat()
            bottom = headerHeight
        }

    private val _calendarGridBounds: RectF = RectF()

    val calendarGridBounds: RectF
        get() = _calendarGridBounds.apply {
            left = timeColumnWidth
            top = headerHeight
            right = viewWidth.toFloat()
            bottom = viewHeight.toFloat()
        }

    private val _weekNumberBounds: RectF = RectF()

    val weekNumberBounds: RectF
        get() = _weekNumberBounds.apply {
            left = 0f
            top = 0f
            right = timeColumnWidth
            bottom = headerHeight
        }

    val weekNumberTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    val weekNumberBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    val hoursPerDay: Int
        get() = maxHour - minHour

    val minutesPerDay: Int
        get() = hoursPerDay * 60

    private val timeRange: IntRange
        get() {
            val includeMidnightHour = showTimeColumnHourSeparators // && showMidnightHour
            val padding = if (includeMidnightHour) 0 else timeColumnHoursInterval
            val startHour = minHour + padding
            return startHour until maxHour
        }

    val displayedHours: IntProgression
        get() = timeRange step timeColumnHoursInterval

    fun getXOriginForDate(date: LocalDate): Float {
        return date.daysFromToday * dayWidth * -1f
    }

    private fun scrollToFirstDayOfWeek() {
        // If the week view is being drawn for the first time, consider the first day of the week.
        val today = LocalDate.now()
        val isWeekView = numberOfVisibleDays >= 7

        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val currentDayIsNotToday = today.dayOfWeek != firstDayOfWeek

        if (isWeekView && currentDayIsNotToday) {
            val difference = today.computeDifferenceWithFirstDayOfWeek()
            currentOrigin.x += dayWidth * difference
        }

        currentOrigin.x = currentOrigin.x.limit(minValue = minX, maxValue = maxX)
    }

    private fun scrollToCurrentTime() {
        val now = LocalDateTime.now()
        val desired = if (now.hour > minHour) {
            // Add some padding above the current time (and thus: the now line)
            now.minusHours(1)
        } else {
            now.minusMinutes(now.minute)
        }

        val hour = min(max(desired.hour, minHour), maxHour)
        val actual = desired.withHour(hour).withMinute(0)

        val fraction = actual.minute / 60f
        val verticalOffset = hourHeight * (actual.hour + fraction)
        val desiredOffset = dayHeight - viewHeight

        currentOrigin.y = min(desiredOffset, verticalOffset) * -1
    }

    /**
     * Returns the provided date, if it is within [minDate] and [maxDate]. Otherwise, it returns
     * [minDate] or [maxDate].
     */
    fun getDateWithinDateRange(date: LocalDate): LocalDate {
        val minDate = minDate ?: date
        val maxDate = maxDate ?: date

        return if (date.isBefore(minDate)) {
            minDate
        } else if (date.isAfter(maxDate)) {
            maxDate.minusDays(numberOfVisibleDays - 1)
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            val diff = date.computeDifferenceWithFirstDayOfWeek()
            date.minusDays(diff)
        } else {
            date
        }
    }

    private fun LocalDate.computeDifferenceWithFirstDayOfWeek(): Int {
        // val firstDayOfWeek = firstDayOfWeek
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val adjuster = TemporalAdjusters.previousOrSame(firstDayOfWeek)
        val startOfWeek = with(adjuster)
        return ChronoUnit.DAYS.between(startOfWeek, this).toInt()
    }

    private fun refreshAfterZooming() {
        if (showCompleteDay) {
            return
        }

        val dayHeight = hourHeight * hoursPerDay
        val isNotFillingEntireHeight = dayHeight < (viewHeight - headerHeight)
        val didZoom = newHourHeight > 0

        if (isNotFillingEntireHeight || didZoom) {
            // Compute a minimum hour height so that users can't zoom out further
            // than the desired hours per day
            val newMinHourHeight = (viewHeight - headerHeight) / hoursPerDay
            val effectiveMinHourHeight = max(minHourHeight, newMinHourHeight)

            newHourHeight = newHourHeight.limit(
                minValue = effectiveMinHourHeight,
                maxValue = maxHourHeight
            )

            currentOrigin.y = currentOrigin.y / hourHeight * newHourHeight
            hourHeight = newHourHeight
            newHourHeight = 0f
        }
    }

    private fun updateVerticalOrigin() {
        // If the new currentOrigin.y is invalid, make it valid.
        val dayHeight = hourHeight * hoursPerDay
        val potentialNewVerticalOrigin = viewHeight - (dayHeight + headerHeight)

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin)
        currentOrigin.y = min(currentOrigin.y, 0f)
    }

    fun getPastBackgroundPaint(isWeekend: Boolean): Paint {
        return if (isWeekend) pastWeekendBackgroundPaint else pastBackgroundPaint
    }

    fun getFutureBackgroundPaint(isWeekend: Boolean): Paint {
        return if (isWeekend) futureWeekendBackgroundPaint else futureBackgroundPaint
    }

    private fun updateHourHeight(viewHeight: Int) {
        hourHeight = (viewHeight - headerHeight) / hoursPerDay
        newHourHeight = hourHeight
    }

    fun refreshHeaderHeight() {
        headerHeight = headerRowPadding + dateLabelHeight

        if (currentAllDayEventHeight > 0) {
            headerHeight += headerRowPadding + currentAllDayEventHeight.toFloat()
        }

        headerHeight += headerRowPadding

        if (showHeaderRowBottomLine) {
            headerHeight += headerRowBottomLinePaint.strokeWidth
        }

        if (showCompleteDay) {
            hourHeight = (viewHeight - headerHeight) / hoursPerDay
            newHourHeight = hourHeight
        }
    }

    fun updateTimeColumnBounds(lineLength: Float, lineHeight: Float) {
        timeColumnTextHeight = lineHeight
        timeColumnWidth = lineLength + timeColumnPadding * 2
    }

    fun update() {
        updateViewState()
        updateScrollState()
        updateDateRange()
    }

    private fun updateScrollState() {
        refreshAfterZooming()
        updateVerticalOrigin()
    }

    private fun updateViewState() {
        if (!isFirstDraw) {
            return
        }

        if (showFirstDayOfWeekFirst) {
            scrollToFirstDayOfWeek()
        }

        if (showCurrentTimeFirst) {
            scrollToCurrentTime()
        }

        isFirstDraw = false
    }

    private fun updateDateRange() {
        val originX = currentOrigin.x

        val daysFromOrigin = ceil(originX / dayWidth).toInt() * (-1)
        startPixel = timeColumnWidth + originX + dayWidth * daysFromOrigin

        // If the user is scrolling, a new view becomes partially visible, so we must add an
        // additional date to the date range
        val isNotScrolling = originX % dayWidth == 0f
        val visibleDays = if (isNotScrolling) numberOfVisibleDays else numberOfVisibleDays + 1

        dateRange.clear()
        val startDate = LocalDate.now().plusDays(daysFromOrigin)
        val newDateRange = startDate.rangeWithDays(visibleDays)
        dateRange += newDateRange.limitTo(minDate, maxDate)

        startPixels.clear()
        startPixels += dateRange.indices.map { startPixel + it * dayWidth }

        dateRangeWithStartPixels.clear()
        dateRangeWithStartPixels += dateRange.zip(startPixels)
    }

    fun onSizeChanged(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height

        if (showCompleteDay) {
            updateHourHeight(height)
        }
    }
}
