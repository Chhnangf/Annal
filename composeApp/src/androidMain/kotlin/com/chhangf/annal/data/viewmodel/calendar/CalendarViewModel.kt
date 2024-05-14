package com.chhangf.annal.data.viewmodel.calendar


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chhangf.annal.data.core.calendar.CalendarIntent
import com.chhangf.annal.data.core.calendar.DateTimeConstants
import com.chhangf.annal.data.core.calendar.Period
import com.chhangf.annal.data.core.calendar.RelativePosition
import com.chhangf.annal.ui.calendar.utils.getNextDates
import com.chhangf.annal.ui.calendar.utils.getRemainingDatesInMonth
import com.chhangf.annal.ui.calendar.utils.getRemainingDatesInWeek
import com.chhangf.annal.ui.calendar.utils.getWeekStartDate
import com.chhangf.annal.ui.calendar.utils.yearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel : ViewModel() {

    private val _visibleDates =
        MutableStateFlow(
            calculateCollapsedCalendarDays(
                // 设置日历开始日期为上周周一
                startDate = LocalDate.now().getWeekStartDate().minusWeeks(1)
            )
        )
    val visibleDates: StateFlow<Array<List<LocalDate>>> = _visibleDates

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    //
    val currentMonth: StateFlow<YearMonth>
        get() = calendarExpanded.zip(visibleDates) { isExpanded, dates ->
            if (isExpanded) {
                dates[RelativePosition.CURRENT.ordinal][dates[RelativePosition.CURRENT.ordinal].size / 2].yearMonth()
            } else {
                if (dates[RelativePosition.CURRENT.ordinal].count { it.month == dates[RelativePosition.CURRENT.ordinal].first().month } > RelativePosition.values().size
                ) {
                    dates[RelativePosition.CURRENT.ordinal].first()
                        .yearMonth()
                } else {
                    dates[RelativePosition.CURRENT.ordinal].last()
                        .yearMonth()
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now().yearMonth())

    //
//
    private val _calendarExpanded = MutableStateFlow(false)
    val calendarExpanded: StateFlow<Boolean> = _calendarExpanded


    fun onIntent(intent: CalendarIntent) {
        when (intent) {
            CalendarIntent.ExpandCalendar -> {
                calculateCalendarDates(
                    startDate = currentMonth.value
                        .minusMonths(1)
                        .atDay(1),
                    period = Period.MONTH
                )
                _calendarExpanded.value = true
            }

            CalendarIntent.CollapseCalendar -> {
                calculateCalendarDates(
                    startDate = calculateCollapsedCalendarVisibleStartDay()
                        .getWeekStartDate()
                        .minusWeeks(1),
                    period = Period.WEEK
                )
                _calendarExpanded.value = false
            }

            is CalendarIntent.LoadNextDates -> {
                calculateCalendarDates(intent.startDate, intent.period)
            }

            is CalendarIntent.SelectDate -> {
                viewModelScope.launch {
                    _selectedDate.emit(intent.date)
                }
            }
        }
    }

    private fun calculateCalendarDates(
        startDate: LocalDate,
        period: Period = Period.WEEK
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _visibleDates.emit(
                when (period) {
                    Period.WEEK -> calculateCollapsedCalendarDays(startDate)
                    Period.MONTH -> calculateExpandedCalendarDays(startDate)
                }
            )
        }
    }

    private fun calculateCollapsedCalendarVisibleStartDay(): LocalDate {
        val halfOfMonth =
            visibleDates.value[RelativePosition.CURRENT.ordinal][visibleDates.value[RelativePosition.CURRENT.ordinal].size / 2]
        val visibleMonth = YearMonth.of(halfOfMonth.year, halfOfMonth.month)
        return if (selectedDate.value.month == visibleMonth.month && selectedDate.value.year == visibleMonth.year)
            selectedDate.value
        else visibleMonth.atDay(1)
    }



    private fun calculateCollapsedCalendarDays(startDate: LocalDate): Array<List<LocalDate>> {
        // startDate -> 21
        val dates =
            startDate.getNextDates(RelativePosition.entries.size * DateTimeConstants.DAYS_IN_WEEK)

        // Group the data into 3 sets of 7 data based on 21 dates.
        return Array(RelativePosition.entries.size) {
            // 1 -> 7, 8 -> 14, 15 -> 21
            dates.slice(it * DateTimeConstants.DAYS_IN_WEEK until (it + 1) * DateTimeConstants.DAYS_IN_WEEK)
        }

    }

    //
    private fun calculateExpandedCalendarDays(startDate: LocalDate): Array<List<LocalDate>> {
        val array = Array(RelativePosition.entries.size) { monthIndex ->
            /**
             *  遍历 上月/本月/下月
             */
            // 当月1号 日期对象，例如 5月1日
            val monthFirstDate = startDate.plusMonths(monthIndex.toLong())
            // 当月末 日期对象，例如 5月31日
            val monthLastDate = monthFirstDate.plusMonths(1).minusDays(1)
            // 当月1号 所处周的周一日期对象，例如 5月1日 是周三，这里返回4月29日 周一
            val weekBeginningDate = monthFirstDate.getWeekStartDate()

            // 当月 1号非周一
            if (weekBeginningDate != monthFirstDate) {
                // 月首周：上月莫过渡到当月1号的List日期对象，例如 5月1日 是周三，这里返回4月29日 - 4月30日
                weekBeginningDate.getRemainingDatesInMonth()
            } else {
                listOf()
            } +
                    //  月中： 全部List日期对象 + 月末所处周剩余List日期对象，例如5月31日周五，这里返回6月1日 - 6月2日
                    monthFirstDate.getNextDates(monthFirstDate.lengthOfMonth()) +

                    // 月末周：本月莫过渡到下月周一的List日期对象，例如 5月31日 是周五，这里返回6月1日 - 6月2日 周日
                    monthLastDate.getRemainingDatesInWeek()
        }
        return array
    }
}
