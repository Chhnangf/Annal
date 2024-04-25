package com.chhangf.annal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chhangf.annal.SessionViewModel
import java.time.LocalTime

@Composable
fun PublishScreen(
    navHostController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var time by remember { mutableStateOf(LocalTime.now()) }
        Card (
            modifier = Modifier
                .width(300.dp).height(400.dp)
        ){
            IosLikeTimePicker(selectedTime = time, onTimeChange = { time = it })
        }


    }
}


@Composable
fun IosLikeTimePicker(
    selectedTime: LocalTime = LocalTime.now(),
    onTimeChange: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(selectedTime.hour) }
    var selectedMinute by remember { mutableStateOf(selectedTime.minute) }

    val scrollStateHour = rememberLazyListState()
    val scrollStateMinute = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "请选择时间",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Hour and Minute Columns
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HourColumn(scrollStateHour, selectedHour) { selectedHour }
            Spacer(modifier = Modifier.width(16.dp))
            MinuteColumn(scrollStateMinute, selectedMinute, { selectedMinute })
        }

        // Confirm button
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val newTime = LocalTime.of(selectedHour, selectedMinute)
                onTimeChange(newTime)
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(text = "确定")
        }
    }
}

@Composable
private fun HourColumn(scrollState: LazyListState, selectedHour: Int, onHourSelected: () -> Unit) {
    val hours = List(24) { it }
    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(hours) { index, hour ->
            HourItem(hour, selectedHour == hour, onHourSelected)
        }
    }
}

@Composable
private fun MinuteColumn(scrollState: LazyListState, selectedMinute: Int, onMinuteSelected: () -> Unit) {
    val minutes = List(60) { it }
    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(minutes) { index, minute ->
            MinuteItem(minute, selectedMinute == minute, onMinuteSelected)
        }
    }
}

@Composable
private fun HourItem(hour: Int, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        backgroundColor = if (isSelected) Color.LightGray else Color.White,
    ) {
        Text(
            text = "$hour",
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun MinuteItem(minute: Int, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        backgroundColor = if (isSelected) Color.LightGray else Color.White,
    ) {
        Text(
            text = if (minute < 10) "0$minute" else "$minute",
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}