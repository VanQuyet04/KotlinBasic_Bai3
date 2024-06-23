package com.example.kotlinbasic_bai3

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmApp()
        }
    }
}

// Hàm AlarmApp, hiển thị giao diện của ứng dụng báo thức
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmApp() {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // State để lưu thời gian được chọn và trạng thái lặp lại của báo thức
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    var isRepeating by remember { mutableStateOf(false) }

    // Scaffold để hiển thị cấu trúc giao diện
    Scaffold(
        topBar = {
            // Thanh top bar của ứng dụng
            TopAppBar(
                title = { Text("Alarm App") }
            )
        },
        content = {
            // Nội dung chính của ứng dụng
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Component chọn thời gian báo thức
                TimePicker(selectedTime = selectedTime, onTimeSelected = { newTime ->
                    selectedTime = newTime
                })
                Spacer(modifier = Modifier.height(16.dp))
                // Checkbox để chọn lặp lại báo thức
                RepeatCheckbox(isRepeating = isRepeating, onCheckedChange = { isChecked ->
                    isRepeating = isChecked
                })
                Spacer(modifier = Modifier.height(16.dp))
                // Button để đặt báo thức
                SetAlarmButton(
                    context = context,
                    alarmManager = alarmManager,
                    selectedTime = selectedTime,
                    isRepeating = isRepeating
                )
            }
        }
    )
}

// Component TimePicker, cho phép chọn giờ và phút
@Composable
fun TimePicker(selectedTime: Calendar, onTimeSelected: (Calendar) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableIntStateOf(selectedTime.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(selectedTime.get(Calendar.MINUTE)) }

    // Giao diện chọn giờ và phút
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Alarm Time",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hour:")
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                enabled = false,
                value = hour.toString(),
                onValueChange = { },
                modifier = Modifier.width(60.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Minute:")
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                enabled = false,
                value = minute.toString(),
                onValueChange = { },
                modifier = Modifier.width(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Button để mở dialog chọn giờ và phút
        Button(
            onClick = {
                showTimePickerDialog(context) { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    onTimeSelected(calendar)
                }
            }
        ) {
            Text("Set Time")
        }
    }
}

// Component Checkbox để chọn lặp lại báo thức
@Composable
fun RepeatCheckbox(isRepeating: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isRepeating,
            onCheckedChange = { isChecked ->
                onCheckedChange(isChecked)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Repeat",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

// Component Button để đặt báo thức
@Composable
fun SetAlarmButton(
    context: Context,
    alarmManager: AlarmManager,
    selectedTime: Calendar,
    isRepeating: Boolean
) {
    Button(
        onClick = {
            setAlarm(context, alarmManager, selectedTime, isRepeating)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Set Alarm")
    }
}

// Hàm show TimePickerDialog để hiển thị dialog chọn giờ và phút
@Suppress("NAME_SHADOWING")
private fun showTimePickerDialog(context: Context, onTimeSet: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Tạo và hiển thị dialog TimePickerDialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSet(hourOfDay, minute)
        },
        hour,
        minute,
        true
    )

    timePickerDialog.show()
}

// Hàm setAlarm để đặt báo thức
@SuppressLint("ScheduleExactAlarm")
private fun setAlarm(
    context: Context,
    alarmManager: AlarmManager,
    selectedTime: Calendar,
    isRepeating: Boolean
) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Đặt báo thức lại hoặc một lần
    if (isRepeating) {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            selectedTime.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            selectedTime.timeInMillis,
            pendingIntent
        )
    }

    // Hiển thị thông báo báo thức đã được đặt
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(selectedTime.time)
    Toast.makeText(context, "Alarm set for $formattedTime", Toast.LENGTH_SHORT).show()
}
