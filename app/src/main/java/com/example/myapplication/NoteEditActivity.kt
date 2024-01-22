package com.example.myapplication

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.myapplication.dao.NoteDao
import com.example.myapplication.database.NotesDatabase
import com.example.myapplication.entities.Note
import com.example.myapplication.util.ImageConverter


class NoteEditActivity : AppCompatActivity(), DialogInterface.OnClickListener {

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
        val btnUploadImage = findViewById<Button>(R.id.editUploadImage)
        val imagePreview = findViewById<ImageView>(R.id.editPreviewImage)

        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        val id = intent.getLongExtra("id", -1)
        if (id >= 0) {
            note = noteDao!!.loadAllByIds(id.toInt())[0]
            val bitmap = note?.image?.let { ImageConverter.convertStringToBase64(it) }
            editTitle?.setText(note?.title)
            editMessage?.setText(note?.message)
            imagePreview.setImageBitmap(bitmap)
        }

        btnSave.setOnClickListener {
            val title = editTitle?.text.toString()
            val message = editMessage?.text.toString()
            val imageString = ImageConverter.convertDrawableToString(imagePreview.drawable)

            if (note != null) {
                note!!.title = title
                note!!.message = message
                note!!.image = imageString
                noteDao?.update(note!!)
            } else {

                noteDao!!.insertAll(Note(title, message, imageString))
            }

            // TODO: @Edwin Add current note here, instead of list
            // Toast.makeText(this, noteDao!!.getAll().toString(), Toast.LENGTH_LONG).show()

            finish()
        }

        val selectImageIntent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                imagePreview.setImageURI(uri)
            }
        btnUploadImage.setOnClickListener { selectImageIntent.launch("image/*") }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.del -> {
                showDeleteDialog()
                true
            }

            R.id.share -> {
                shareNote()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes), this)
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // TODO share not working when the note is not saved in database.
    //  Same problem when you edit a share.
    //  Only text share.
    private fun shareNote() {
        if (note == null) {
            Toast.makeText(this, getString(R.string.share_not_saved), Toast.LENGTH_LONG).show()
        } else {
            val sendTitle = getString(R.string.share_title) + (note?.title ?: "")
            val sendMessage = getString(R.string.share_message) + (note?.message ?: "")
            val shareBody = "$sendTitle\n$sendMessage"
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_choose)))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        note?.let {
            noteDao?.delete(it)

            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_LONG).show()

            finish()
        }
    }
}
