package com.example.myapplication.util

import android.content.Context

class Preferences constructor(context: Context) {

    companion object {
        const val PREFERENCES_NOTE_TITLE = "note_title"
        const val PREFERENCES_NOTE_MESSAGE = "note_message"
        const val PREFERENCES_NAME = "preferences_note"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setNoteTitle(noteTitle: String?) {
        preferences.edit().putString(PREFERENCES_NOTE_TITLE, noteTitle).apply()
    }

    fun getNoteTitle(): String? {
        return preferences.getString(PREFERENCES_NOTE_TITLE, null)
    }

    fun setNoteMessage(noteMessage: String?) {
        preferences.edit().putString(PREFERENCES_NOTE_MESSAGE, noteMessage).apply()
    }

    fun getNoteMessage(): String? {
        return preferences.getString(PREFERENCES_NOTE_MESSAGE, null)
    }

    fun removeNoteTitle() {
        preferences.edit().remove(PREFERENCES_NOTE_TITLE).apply()
    }

    fun removeNoteMessage() {
        preferences.edit().remove(PREFERENCES_NOTE_MESSAGE).apply()
    }
}