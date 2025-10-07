package com.skhaftin.model

data class ScheduledDonation(
    val scheduleId: String = "",
    val day: String = "",
    val time: String = "",
    val foodItem: String = "",
    val repeat: Boolean = false
)
