package com.example.data.firestore

data class ContentReport(
    val id: String = "",
    val reporterUid: String = "",
    val reporterName: String = "",
    val questionId: String = "",
    val questionText: String = "",
    val classId: String = "",
    val subjectId: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "open",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val adminNote: String = ""
)

data class AdminActivityEvent(
    val id: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val action: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ContentQualityIssue(
    val severity: String,
    val category: String,
    val entityType: String,
    val entityId: String,
    val description: String
)

data class AdminGovernanceState(
    val reports: List<ContentReport> = emptyList(),
    val isLoadingReports: Boolean = false,
    val reportsError: String? = null,
    val activities: List<AdminActivityEvent> = emptyList(),
    val isLoadingActivities: Boolean = false,
    val activitiesError: String? = null,
    val qualityIssues: List<ContentQualityIssue> = emptyList(),
    val isScanningQuality: Boolean = false,
    val qualityError: String? = null,
    val updatingReportId: String? = null
)
