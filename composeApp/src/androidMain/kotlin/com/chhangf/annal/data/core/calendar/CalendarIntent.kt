package com.chhangf.annal.data.core.calendar

import java.time.LocalDate


sealed class CalendarIntent {

    // 子类，表达对日历的加载操作意图，具备2个属性，startDate表示起始日期，type表示日历类型
    class LoadNextDates(
        val startDate: LocalDate,
        val period: Period = Period.WEEK
    ): CalendarIntent()
    class SelectDate(val date: LocalDate): CalendarIntent()

    // 数据实例，表达对日历的展开/收起操作意图
    data object ExpandCalendar: CalendarIntent()
    data object CollapseCalendar: CalendarIntent()
}

