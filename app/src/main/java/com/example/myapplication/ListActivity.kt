package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.myapplication.adapters.NoteAdapter
import com.example.myapplication.dao.NoteDao
import com.example.myapplication.database.NotesDatabase
import com.example.myapplication.entities.Note
import com.google.android.material.snackbar.Snackbar


class ListActivity : AppCompatActivity() {
    // This activity is responsible for listing all notes.
    private lateinit var noteDao: NoteDao
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var rvNotes: RecyclerView
    private lateinit var noNoteTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout content to the activity_list layout.
        setContentView(R.layout.activity_list)

        // Set up the main toolbar.
        setSupportActionBar(findViewById(R.id.tbMain))

        rvNotes = findViewById(R.id.lvNotes)
        noNoteTextView = findViewById(R.id.noNote)
        noNoteTextView.visibility = View.GONE

        // Initializes database
        initDatabase()
        // Initializes NoteAdapter
        initAdapter()
        // Added swipe right to delete functionality
        swipeToDeleteListener()
    }

    // Initializes database
    fun initDatabase() {
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
    }

    // Initializes NoteAdapter
    fun initAdapter() {
        noteAdapter = NoteAdapter(this, noteDao.getAll())
        rvNotes.adapter = noteAdapter

        noteAdapter.setOnClickListener(object :
            NoteAdapter.OnClickListener {
            override fun onClick(position: Int) {
                val intent = Intent(this@ListActivity, NoteEditActivity::class.java)
                intent.putExtra("id", noteAdapter.notes.get(position).id)
                startActivity(intent)
            }
        })
    }

    fun swipeToDeleteListener() {
        // Swipe right to delete note helper. RecyclerView is used to enable this functionality
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            // Handle the swipe for each note.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Saves deleted note to variable, if user decides to undo
                val deletedNote: Note =
                    noteAdapter.notes.get(viewHolder.adapterPosition)
                val position = viewHolder.adapterPosition
                noteAdapter.notes.removeAt(viewHolder.adapterPosition)
                noteAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                // Delete the note from the database.
                noteDao.delete(deletedNote)
                noteAdapter.notifyDataSetChanged()
                // Show a snackbar with undo option.
                Snackbar.make(
                    rvNotes,
                    resources.getString(R.string.note_deleted) + ": " + deletedNote.title,
                    Snackbar.LENGTH_LONG
                )    // If undo clicked, insert the note back at the right position.
                    .setAction(
                        resources.getString(R.string.undo),
                        View.OnClickListener {
                            noteAdapter.notes.add(position, deletedNote)
                            noteAdapter.notifyItemInserted(position)
                            noteDao.insertAll(deletedNote)
                        }).show()
            }
            // Attach the ItemTouchHelper to the RecyclerView.
        }).attachToRecyclerView(rvNotes)
    }

    override fun onResume() {
        super.onResume()
        // Reload notes and refresh the adapter when resuming the activity.
        val notesList = noteDao.getAll()
        noteAdapter.notes = notesList
        noteAdapter.notifyDataSetChanged()
        // Show or hide the no note text view based on notes availability.
        if (notesList.isEmpty()) {
            noNoteTextView.visibility = View.VISIBLE
        } else {
            noNoteTextView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu, this adds the items to the action bar if it's present.
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection.
        if (item.itemId == R.id.add) {
            // Display a toast message and start the NoteEditActivity if add button is selected.
            Toast.makeText(this, getString(R.string.add), Toast.LENGTH_LONG).show()

            val intent = Intent(this, NoteEditActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}