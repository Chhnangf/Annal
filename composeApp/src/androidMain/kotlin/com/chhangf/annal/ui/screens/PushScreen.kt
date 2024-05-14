package com.chhangf.annal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.chhangf.annal.SessionViewModel
import com.chhangf.annal.data.viewmodel.calendar.CalendarViewModel
import com.chhangf.annal.ui.calendar.ExpandableCalendar
import com.chhangf.annal.ui.theme.CalendarTheme
import com.chhangf.annal.ui.theme.calendarDefaultTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
        CalendarTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),

                ) {
                Calendar()
            }
        }

    }
}


@Composable
fun Calendar() {
    val currentDate = remember { mutableStateOf(LocalDate.now()) }
    val viewModel: CalendarViewModel = viewModel()
    val loadedDates = viewModel.visibleDates.collectAsState()
    val scrollState = rememberScrollState()
    Column(Modifier.verticalScroll(scrollState)) {
        ExpandableCalendar(
            theme = calendarDefaultTheme.copy(
                dayShape = CircleShape,
                backgroundColor = Color.Black,
                selectedDayBackgroundColor = Color.White,
                dayValueTextColor = Color.White,
                selectedDayValueTextColor = Color.Black,
                headerTextColor = Color.White,
                weekDaysTextColor = Color.White
            ), onDayClick = {
                currentDate.value = it
            })

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column {
                Text("Selected date: ${currentDate.value}")
                Spacer(modifier = Modifier.height(8.dp))
                loadedDates.value.forEach { date ->
                    Text("date Range: $date")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

    }
}