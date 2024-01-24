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
    private lateinit var noteDao: NoteDao
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var rvNotes: RecyclerView
    private lateinit var noNoteTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        setSupportActionBar(findViewById(R.id.tbMain))

        noNoteTextView = findViewById(R.id.noNote)
        noNoteTextView.visibility = View.GONE

        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
        rvNotes = findViewById(R.id.lvNotes)
        noteAdapter = NoteAdapter(this, noteDao!!.getAll())
        rvNotes.adapter = noteAdapter

        noteAdapter.setOnClickListener(object :
            NoteAdapter.OnClickListener {
            override fun onClick(position: Int) {
                val intent = Intent(this@ListActivity, NoteEditActivity::class.java)
                intent.putExtra("id", noteAdapter.notes.get(position).id)
                startActivity(intent)
            }
        })



        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedNote: Note =
                    noteAdapter.notes.get(viewHolder.adapterPosition)
                val position = viewHolder.adapterPosition
                noteAdapter.notes.removeAt(viewHolder.adapterPosition)
                noteAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                noteDao.delete(deletedNote)
                noteAdapter.notifyDataSetChanged()
                Snackbar.make(
                    rvNotes,
                    resources.getString(R.string.note_deleted) + ": " + deletedNote.title,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(
                        resources.getString(R.string.undo),
                        View.OnClickListener {
                            noteAdapter.notes.add(position, deletedNote)
                            noteAdapter.notifyItemInserted(position)
                            noteDao.insertAll(deletedNote)
                        }).show()
            }
        }).attachToRecyclerView(rvNotes)

    }

    override fun onResume() {
        super.onResume()
        val notesList = noteDao.getAll()
        noteAdapter.notes = notesList
        noteAdapter.notifyDataSetChanged()
        if (notesList.isEmpty()) {
            noNoteTextView.visibility = View.VISIBLE
        } else {
            noNoteTextView.visibility = View.GONE
        }


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