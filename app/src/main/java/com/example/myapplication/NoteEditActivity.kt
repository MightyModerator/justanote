package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.Room


class NoteEditActivity : AppCompatActivity() {
    // private var preferences: Preferences? = null

    private var noteDao: NoteDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        setSupportActionBar(findViewById(R.id.tbEdit))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // preferences = Preferences(this)
        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editMessage = findViewById<EditText>(R.id.editMessage)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        // Set OnClickListener
        btnSave.setOnClickListener {
            val title = editTitle?.text.toString()
            val message = editMessage?.text.toString()
            noteDao!!.insertAll(Note(title, message))
            // preferences?.setNoteTitle(editTitle.text.toString())
            // preferences?.setNoteMessage(editMessage.text.toString())
            // Toast.makeText(this, getString(R.string.note_saved), Toast.LENGTH_LONG).show()
            Toast.makeText(this, noteDao!!.getAll().toString(), Toast.LENGTH_LONG).show()
            finish()
        }

        // Vorbelegen von Titel und Nachricht
        // editTitle.setText(preferences?.getNoteTitle())
        // editMessage.setText(preferences?.getNoteMessage())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.del -> {
                showDeleteDialog()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes)) { dialog, id ->
                // preferences?.setNoteTitle(null)
                // preferences?.setNoteMessage(null)
                Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
