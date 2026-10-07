package com.notiguard.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class TimeRangeTest {

    @Test fun rangesStepBackOnTheLocalCalendar() {
        val now = at(2026, Calendar.MARCH, 15)
        assertDate(TimeRange.WEEK.since(now), 2026, Calendar.MARCH, 8)
        assertDate(TimeRange.MONTH.since(now), 2026, Calendar.FEBRUARY, 15)
        assertDate(TimeRange.THREE_MONTHS.since(now), 2025, Calendar.DECEMBER, 15)
        assertDate(TimeRange.HALF_YEAR.since(now), 2025, Calendar.SEPTEMBER, 15)
        assertDate(TimeRange.YEAR.since(now), 2025, Calendar.MARCH, 15)
    }

    @Test fun monthCrossesTheYearBoundary() {
        assertDate(TimeRange.MONTH.since(at(2026, Calendar.JANUARY, 15)), 2025, Calendar.DECEMBER, 15)
    }

    @Test fun allHasNoLowerBoundAndUnknownStorageFallsBackToAWeek() {
        assertEquals(0L, TimeRange.ALL.since())
        assertEquals(TimeRange.WEEK, TimeRange.fromStored(null))
        assertEquals(TimeRange.WEEK, TimeRange.fromStored("forever"))
        assertEquals(TimeRange.HALF_YEAR, TimeRange.fromStored("HALF_YEAR"))
    }

    private fun at(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 15, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun assertDate(millis: Long, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        assertEquals(year, calendar.get(Calendar.YEAR))
        assertEquals(month, calendar.get(Calendar.MONTH))
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(15, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(Calendar.MINUTE))
    }
}
