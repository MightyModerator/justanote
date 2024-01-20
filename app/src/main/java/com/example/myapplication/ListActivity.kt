package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room

private var noteDao: NoteDao? = null

class ListActivity : AppCompatActivity() {
    // private var tvTitle: TextView? = null
    // private var tvMessage: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        setSupportActionBar(findViewById(R.id.tbMain))

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        // tvTitle = findViewById(R.id.tvTitle)
        // tvMessage = findViewById(R.id.tvMessage)

        val lvNotes = findViewById<ListView>(R.id.lvNotes)
        // val notes = arrayOf("test", "test1", "test2")
        // val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notes)
        // lvNotes.adapter = arrayAdapter

        val adapter = NoteAdapter(this, noteDao!!.getAll())
        lvNotes.adapter = adapter

    }

    override fun onResume() {
        super.onResume()

        //tvTitle!!.text = Preferences(this).getNoteTitle()
        //tvMessage!!.text = Preferences(this).getNoteMessage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {
            Toast.makeText(this, getString(R.string.add), Toast.LENGTH_LONG).show()

            val intent = Intent(this, NoteEditActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}