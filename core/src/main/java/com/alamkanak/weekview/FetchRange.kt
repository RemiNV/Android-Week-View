package com.alamkanak.weekview

import java.time.LocalDate
import java.time.YearMonth

internal data class FetchRange(
    val previous: YearMonth,
    val current: YearMonth,
    val next: YearMonth
) {

    val periods: List<YearMonth> = listOf(previous, current, next)

    internal companion object {
        fun create(firstVisibleDate: LocalDate): FetchRange {
            val month = YearMonth.of(firstVisibleDate.year, firstVisibleDate.month)
            return FetchRange(month.minusMonths(1), month, month.plusMonths(1))
        }
    }
}
