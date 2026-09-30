package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DeshExamDao
import com.example.data.local.TopicMistakeCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeaknessInsightViewModel(private val dao: DeshExamDao) : ViewModel() {

    private val _weakTopics = MutableStateFlow<List<TopicMistakeCount>>(emptyList())
    val weakTopics: StateFlow<List<TopicMistakeCount>> = _weakTopics

    init {
        loadWeakTopics()
    }

    private fun loadWeakTopics() {
        viewModelScope.launch {
            _weakTopics.value = dao.getTopicMistakeCounts()
        }
    }
}
