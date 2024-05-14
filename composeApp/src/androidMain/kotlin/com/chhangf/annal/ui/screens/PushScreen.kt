package com.chhangf.annal.ui.screens

import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.data.viewmodel.ToDoViewModel
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun PublishScreen(
    navHostController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            Modifier.fillMaxWidth().height(100.dp).padding(top = 10.dp).background(Color.Black)
        ) {

        }
        Spacer(Modifier.height(10.dp))
        ExpandableBox()

    }


}


@Composable
fun ExpandableBox() {

    // 初始高度和最大扩展高度
    var initialHeight by remember { mutableStateOf(100f) }
    val maxHeight = 400f
    var animateHeight by remember { mutableStateOf(initialHeight) }
    fun updateHeight(height: Float) {
        animateHeight = height
    }

    // val thresholdHeight = 200f // 高度阈值，超过此高度显示30天日历
    val dragThreshold = maxHeight * 0.6f // 设定40%作为动画触发的拖动阈值

    // calendar type
    var calendarType by remember { mutableStateOf("7") }
    fun updateCalendarType(height: Float) {
        calendarType = if (height >= initialHeight + dragThreshold) "30" else "7"
    }

    var showAnimate by remember { mutableStateOf(false) }

    val animateAction = animateDpAsState(
        targetValue = animateHeight.dp,
        animationSpec = tween(
            durationMillis = 1000, // 指定动画持续时间，例如500毫秒
            easing = LinearOutSlowInEasing // 线性进出的插值器，实现线性动画效果
        )
    )

    var totalDelta by remember { mutableStateOf(0f) } // 记录delta
    // 可拖动状态
    val draggableState = remember {
        DraggableState { delta ->
            totalDelta += delta
            Log.d("ExpandableBox", "totalDelta=$totalDelta, dragThreshold=$dragThreshold")
            // 如果拖动方向向下并且未达到最大高度，则增加高度
            if (totalDelta > dragThreshold) {
                totalDelta = dragThreshold
                animateHeight = maxHeight
            }
            // 如果拖动方向向上且高度大于初始高度，则减小高度直到初始高度
            else if (delta < 0) {
                totalDelta = 0f
                animateHeight = initialHeight
            }

            updateHeight((animateHeight + totalDelta / 4).coerceIn(initialHeight, maxHeight / 1.5f))
            updateCalendarType(animateHeight + totalDelta / 4)
        }
    }


    Column {
        Row(
            Modifier
                .fillMaxWidth() // 内部Box充满水平空间
                .height(

                    animateAction.value

                )
                .background(Color.Gray.copy(0.2f)).border(1.dp, Color.Black)
        ) {
            DynamicCalendar(calendarType)
        }

    }

    Column(
        Modifier
            .fillMaxWidth().height(30.dp)
            .background(Color.Green.copy(0.1f))
            .draggable(
                orientation = Orientation.Vertical,
                state = draggableState,
                onDragStopped = {
                    delay(100L)
                    // 当拖动停止时，确保高度不超过最大值也不低于初始值
                    if (animateAction.value < (initialHeight.dp + dragThreshold.dp)) {
                        animateHeight = initialHeight
                    }
                    updateCalendarType(animateHeight)
                }
            ),

        ) {
        Text("拖动这个！")
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 函数用于根据高度更新calendarType


}


@Composable
private fun DayBlock(modifier: Modifier = Modifier) {
    var isChecked by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(20.dp) // 每个方块的大小
            .background(Color.LightGray).border(1.dp, Color.White)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
    }
}

@Composable
fun DynamicCalendar(calendarType: String) {
    val daysToShow = if (calendarType == "30") getThisMonthDates().size else 7
    val daysPerRow = 7 // 每行固定7个block

    // 计算行数时确保至少有一行，特别是当daysToShow为7时
    val rowCount = when {
        daysToShow <= daysPerRow -> 1
        else -> ((daysToShow - 1) / daysPerRow) + 1
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(rowCount) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 每行显示的DayBlock数量
                val blocksInThisRow = if (rowIndex == rowCount - 1 && daysToShow != 7) {
                    // 最后一行，显示剩余天数
                    daysToShow % daysPerRow
                } else {
                    // 其他行，每行满7个
                    daysPerRow
                }
                repeat(blocksInThisRow) {
                    DayBlock()
                    Spacer(modifier = Modifier.width(10.dp)) // 自定义间距
                }
            }
        }
    }
}

fun getThisWeekDates(): List<LocalDate> {
    val startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return (0..6).map { startOfWeek.plusDays(it.toLong()) }
}

fun getThisMonthDates(): List<LocalDate> {
    val monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    val monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
    return generateSequence(monthStart) { date -> date.plusDays(1) }
        .takeWhile { it.isBefore(monthEnd.plusDays(1)) }
        .toList()
}

@Composable
fun CustomSmoothCalendar(
    todoViewModel: ToDoViewModel,
    onDateSelected: (LocalDate) -> Unit,
    selectedDate: LocalDate
) {
    val initialHeight = 35f
    val maxHeight = 310f
    val dragThreshold = maxHeight * 0.2f
    var varAnimiteH by remember { mutableStateOf(initialHeight) }
    var isExpanding by remember { mutableStateOf(false) }
    var calendarType by remember { mutableStateOf("week") }

    val animateAction = animateFloatAsState(
        targetValue = varAnimiteH,
        animationSpec = SpringSpec(stiffness = 500f)
    )

    Box(
        modifier = Modifier
            .height(with(LocalDensity.current) { animateAction.value.toDp() })
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    varAnimiteH += delta.toFloat()
                    isExpanding = varAnimiteH >= initialHeight + dragThreshold
                    if (isExpanding) {
                        calendarType = "month"
                    } else {
                        calendarType = "week"
                    }
                    // 添加边界检查，确保高度不会超出设定范围
                    varAnimiteH = varAnimiteH.coerceIn(initialHeight, maxHeight)
                },
                onDragStopped = { velocity ->
                    // 可以根据需要调整释放时的逻辑，这里简化处理
                }
            )
    ) {
        // 根据calendarType显示不同的日历视图
        if (calendarType == "week") {
            DisplayCurrentWeekDates(todoViewModel, onDateSelected, selectedDate, LocalDate.now())
        } else if (calendarType == "month") {
            DisplayCurrentMonthDates(
                todoViewModel,
                onDateSelected,
                selectedDate,
                LocalDate.now()
            )
        }
    }
}