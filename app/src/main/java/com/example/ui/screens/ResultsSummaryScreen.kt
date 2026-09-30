package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ResultsSummaryScreen() {
    val context = LocalContext.current
    val finalScore = 95 // Example

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ফলাফল সারসংক্ষেপ",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "আমি DeshExam কুইজে $finalScore স্কোর পেয়েছি! তুমি ভালো করতে পারবে কি?")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }) {
                Icon(Icons.Default.Share, contentDescription = "শেয়ার করুন")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "স্কোর: $finalScore", style = MaterialTheme.typography.bodyLarge)
        
        // Charting component temporarily removed due to library import issues
    }
}
