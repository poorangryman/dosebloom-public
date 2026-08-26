package com.dosebloom.app

data class Medicine(
    val id: Long = 0,
    val name: String,
    val dose: String,
    val unit: String,
    val times: List<String>,
    val startDate: String,
    val endDate: String,
    val note: String,
    val stock: Int,
    val lowStock: Int,
    val asNeeded: Boolean,
    val profile: String
)

data class Intake(
    val id: Long,
    val medicineId: Long,
    val date: String,
    val plannedTime: String,
    val actualMillis: Long,
    val status: String
)
