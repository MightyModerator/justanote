package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.entities.Note

@Dao

interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAll(): MutableList<Note>

    @Insert
    fun insertAll(vararg notes: Note)

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id IS (:id)")
    fun loadAllByIds(id: Int): MutableList<Note>

    @Update
    fun update(note: Note)
}
