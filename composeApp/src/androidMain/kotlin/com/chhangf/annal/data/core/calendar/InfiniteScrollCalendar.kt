package com.chhangf.annal.data.core.calendar

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chhangf.annal.data.viewmodel.todo.ToDoViewModel
import java.time.LocalDate
import java.time.YearMonth

class InfiniteScrollCalendar(
    private var selectDate: YearMonth = YearMonth.now(),
    private val toDoViewModel: ToDoViewModel,
    ) {
    // 存储当前年月日,暴露给ui层


    private val cache = mutableMapOf<YearMonth, List<LocalDate>>()
    private var _displayedDates by mutableStateOf(emptyList<LocalDate>()) // 添加可观察状态
    val displayedDates: List<LocalDate>
        get() = _displayedDates

    // 新增方法，用于更新显示的日期并触发UI更新
    private fun updateDisplayedDates(newDates: List<LocalDate>) {
        _displayedDates = newDates
    }


    init {
        // 预加载当前月的日期，确保displayedDates至少有初始值
        val currentMonthDates = getAndCacheDates(selectDate)
        updateDisplayedDates(currentMonthDates)
        preloadDates()
        toDoViewModel.updateCurrentDate(selectDate)
    }

    private fun preloadDates() {
        // 预加载当前月前后各一月的日期作为缓冲
        val prevMonth = selectDate.minusMonths(1)
        val nextMonth = selectDate.plusMonths(1)
        getAndCacheDates(prevMonth)
        getAndCacheDates(nextMonth)
    }

    // 缓存上月和下月日历对象
    private fun getAndCacheDates(month: YearMonth): List<LocalDate> {
        Log.d("getAndCacheDates -> ","month=$month, ${toDoViewModel.getContinuousMonthDates(month)}")

        toDoViewModel.updateCurrentDate(month)
        return cache.getOrPut(month) {
            toDoViewModel.getContinuousMonthDates(month)
        }
    }

    fun getCurrentDisplayDates(): List<LocalDate> = displayedDates

//    fun handleScrollDirection(direction: ScrollDirection) {
//        when (direction) {
//            ScrollDirection.LEFT -> {
//                // 直接滚动到上个月并加载数据，无需外部调用
//                scrollLeft()
//            }
//            ScrollDirection.RIGHT -> {
//                // 直接滚动到下个月并加载数据，无需外部调用
//                scrollRight()
//            }
//        }
//    }


//    private fun scrollLeft() {
//        // 更新currentDate到上一个月，并自动加载数据
//        currentDate = currentDate.minusMonths(1)
//        preloadAdjacentMonths() // 自动加载上个月和下个月数据
//        updateDisplayedDates(getAndCacheDates(currentDate)) // 更新显示的日期
//    }
//
//    private fun scrollRight() {
//        // 更新currentDate到下一个月，并自动加载数据
//        currentDate = currentDate.plusMonths(1)
//        preloadAdjacentMonths() // 自动加载上个月和下个月数据
//        updateDisplayedDates(getAndCacheDates(currentDate)) // 更新显示的日期
//    }

//    private fun preloadAdjacentMonths() {
//        val prevMonth = currentDate.minusMonths(1)
//        val nextMonth = currentDate.plusMonths(1)
//        getAndCacheDates(prevMonth) // 预加载上个月
//        getAndCacheDates(nextMonth) // 预加载下个月
//    }

}

//enum class ScrollDirection {
//    LEFT,
//    RIGHT
//}