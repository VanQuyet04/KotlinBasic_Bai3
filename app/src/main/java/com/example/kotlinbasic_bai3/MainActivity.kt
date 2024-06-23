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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import androidx.compose.ui.text.input.TextFieldValue
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmApp()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmApp() {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    var isRepeating by remember { mutableStateOf(false) }
    var pendingIntent by remember { mutableStateOf<PendingIntent?>(null) }
    val alarmTimeTextState = remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm App") }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(selectedTime) { newTime ->
                    selectedTime = newTime
                    updateAlarmTimeText(selectedTime, alarmTimeTextState)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRepeating,
                        onCheckedChange = { isChecked ->
                            isRepeating = isChecked
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Repeat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        setAlarm(context, alarmManager, selectedTime, isRepeating)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Alarm")
                }
            }
        }
    )
}

@SuppressLint("AutoboxingStateCreation")
@Composable
fun TimePicker(selectedTime: Calendar, onTimeSelected: (Calendar) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableStateOf(selectedTime.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(selectedTime.get(Calendar.MINUTE)) }

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

                value = hour.toString(),
                onValueChange = { if (it.isNotBlank()) hour = it.toInt() },
                modifier = Modifier.width(60.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Minute:")
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = minute.toString(),
                onValueChange = { if (it.isNotBlank()) minute = it.toInt() },
                modifier = Modifier.width(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
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

private fun showTimePickerDialog(context: Context, onTimeSet: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

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

@SuppressLint("ScheduleExactAlarm")
fun setAlarm(
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

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(selectedTime.time)
    Toast.makeText(context, "Alarm set for $formattedTime", Toast.LENGTH_SHORT).show()
}

private fun updateAlarmTimeText(selectedTime: Calendar, alarmTimeTextState: MutableState<TextFieldValue>) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(selectedTime.time)
    alarmTimeTextState.value = TextFieldValue(formattedTime)
}


