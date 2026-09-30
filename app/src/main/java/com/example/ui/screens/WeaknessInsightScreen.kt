package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.WeaknessInsightViewModel

@Composable
fun WeaknessInsightScreen(
    viewModel: WeaknessInsightViewModel,
    onStartRevision: () -> Unit
) {
    val weakTopics by viewModel.weakTopics.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "দুর্বলতার বিশ্লেষণ",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (weakTopics.isEmpty()) {
            Text(text = "এখনো কোনো দুর্বলতা চিহ্নিত হয়নি! অনুশীলন চালিয়ে যান।")
        } else {
            Text(text = "যেসব টপিকে মনোযোগ দিতে হবে:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weakTopics) { topicMistake ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "টপিক আইডি: ${topicMistake.topicId}")
                            Text(text = "ভুলের সংখ্যা: ${topicMistake.mistakeCount}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartRevision,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "পুনরীক্ষণ টেস্ট শুরু করুন")
            }
        }
    }
}
