package com.example.myapplication.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Base64

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "longitude") var longitude: String,
    @ColumnInfo(name = "latitude") var latitude: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
