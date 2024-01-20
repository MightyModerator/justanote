package com.example.myapplication

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.example.myapplication.dao.NoteDao
import com.example.myapplication.database.NotesDatabase
import com.example.myapplication.entities.Note

class NoteEditActivity : AppCompatActivity(), DialogInterface.OnClickListener {

    // private var preferences: Preferences? = null
    private var noteDao: NoteDao? = null
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        setSupportActionBar(findViewById(R.id.tbEdit))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editMessage = findViewById<EditText>(R.id.editMessage)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        val id = intent.getLongExtra("id", -1)
        if (id >= 0) {
            note = noteDao!!.loadAllByIds(id.toInt())[0]
            editTitle?.setText(note?.title)
            editMessage?.setText(note?.message)
        }
        // Set OnClickListener
        btnSave.setOnClickListener {
            val title = editTitle?.text.toString()
            val message = editMessage?.text.toString()

            if (note != null) {
                note!!.title = title
                note!!.message = message
                noteDao?.update(note!!)
            } else {
                noteDao!!.insertAll(Note(title, message))
            }

            // Show toast for user
            Toast.makeText(this, noteDao!!.getAll().toString(), Toast.LENGTH_LONG).show()

            finish()
        }

        // Vorbelegen von Titel und Nachricht
        // editTitle.setText(preferences?.getNoteTitle())
        // editMessage.setText(preferences?.getNoteMessage())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.del -> showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.note_deleted))
            .setPositiveButton(getString(R.string.yes), this)
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        note?.let {
            noteDao?.delete(it)

            // Display Toast
            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_LONG).show()

            finish()
        }
    }
}
