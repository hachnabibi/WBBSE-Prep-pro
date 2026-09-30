package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    totalSeconds: Long,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableLongStateOf(totalSeconds) }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            onFinish()
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    Text(
        text = formattedTime,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = if (timeLeft < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
