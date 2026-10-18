package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // ADMIN, POS_USER, VISITOR
    val mobile: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
