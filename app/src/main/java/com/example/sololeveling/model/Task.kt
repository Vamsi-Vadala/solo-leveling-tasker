package com.example.sololeveling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val day: String,  // Day of the week (e.g., "Monday")
    var isChecked: Boolean = false
)
