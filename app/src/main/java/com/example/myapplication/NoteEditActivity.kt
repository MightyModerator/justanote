package com.example.myapplication

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.myapplication.dao.NoteDao
import com.example.myapplication.database.NotesDatabase
import com.example.myapplication.entities.Note
import com.example.myapplication.util.ImageConverter


class NoteEditActivity : AppCompatActivity(), DialogInterface.OnClickListener, LocationListener {

    private var noteDao: NoteDao? = null
    private var note: Note? = null

    private lateinit var editTitle: EditText
    private lateinit var editMessage: EditText
    private lateinit var locationManager: LocationManager
    private lateinit var tvLongitude: TextView
    private lateinit var tvLatitude: TextView
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        setSupportActionBar(findViewById(R.id.tbEdit))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        editTitle = findViewById(R.id.editTitle)
        editMessage = findViewById(R.id.editMessage)
        tvLongitude = findViewById(R.id.editLongitude)
        tvLatitude = findViewById(R.id.editLatitude)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnUploadImage = findViewById<Button>(R.id.editUploadImage)
        val btnGetLocation = findViewById<Button>(R.id.editGetLocation)
        val imagePreview = findViewById<ImageView>(R.id.editPreviewImage)

        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        val id = intent.getIntExtra("id", -1)
        if (id >= 0) {
            note = noteDao!!.loadAllByIds(id.toInt())[0]
            val bitmap = note?.image?.let { ImageConverter.convertStringToBase64(it) }
            editTitle?.setText(note?.title)
            editMessage?.setText(note?.message)
            tvLongitude.setText(note?.longitude)
            tvLatitude.setText(note?.latitude)
            imagePreview.setImageBitmap(bitmap)
        }

        btnSave.setOnClickListener {
            val title = editTitle?.text.toString()
            val message = editMessage?.text.toString()
            val imageString = ImageConverter.convertDrawableToString(imagePreview.drawable)
            val longitude = tvLongitude?.text.toString()
            val latitude = tvLatitude?.text.toString()

            if (note != null) {
                note!!.title = title
                note!!.message = message
                note!!.image = imageString
                note!!.longitude = longitude
                note!!.latitude = latitude
                noteDao?.update(note!!)
            } else {
                noteDao!!.insertAll(Note(title, message, imageString, longitude, latitude))
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
        btnGetLocation.setOnClickListener { getLocation() }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.location -> {
                startMapsActivity()
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

    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("longitude", tvLongitude.text)
        intent.putExtra("latitude", tvLatitude.text)
        startActivity(intent)
    }

    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        tvLongitude = findViewById(R.id.editLongitude)
        tvLatitude = findViewById(R.id.editLatitude)
        tvLongitude.text = location.longitude.toString()
        tvLatitude.text = location.latitude.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_granted),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes), this)
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun shareNote() {
        val currentTitle = editTitle.text.toString()
        val currentMessage = editMessage.text.toString()

        val isModified = note?.title != currentTitle || note?.message != currentMessage

        if (isModified) {
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
