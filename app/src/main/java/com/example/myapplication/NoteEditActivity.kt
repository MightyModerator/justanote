package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class NoteEditActivity : AppCompatActivity(), DialogInterface.OnClickListener {

    // Note variables
    private var noteDao: NoteDao? = null
    private var note: Note? = null

    // View variables
    private lateinit var editTitle: EditText
    private lateinit var editMessage: EditText
    private lateinit var tvLongitude: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var btnSave: Button
    private lateinit var btnUploadImage: Button
    private lateinit var btnGetLocation: Button
    private lateinit var imagePreview: ImageView

    // Location variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        setSupportActionBar(findViewById(R.id.tbEdit))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        // Initialize elements of layout file
        initializeViews()
        // Initialize database and get note data access object
        initializeDatabase()
        // Checks if note already exists and set data
        getExistingNote()

        // Image selection
        val selectImageIntent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                imagePreview.setImageURI(uri)
            }
        btnUploadImage.setOnClickListener { selectImageIntent.launch("image/*") }

        // Location button
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        btnGetLocation.setOnClickListener { getLocation() }

        // Saves note on button click
        btnSave.setOnClickListener { saveNote() }
    }

    private fun initializeViews() {
        editTitle = findViewById(R.id.editTitle)
        editMessage = findViewById(R.id.editMessage)
        tvLongitude = findViewById(R.id.editLongitude)
        tvLatitude = findViewById(R.id.editLatitude)
        btnSave = findViewById(R.id.btnSave)
        btnUploadImage = findViewById(R.id.editUploadImage)
        btnGetLocation = findViewById(R.id.editGetLocation)
        imagePreview = findViewById(R.id.editPreviewImage)
    }

    private fun initializeDatabase() {
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
    }

    private fun getExistingNote() {
        val id = intent.getIntExtra("id", -1)
        if (id >= 0) {
            note = noteDao?.loadAllByIds(id.toInt())?.firstOrNull()
            note?.let {
                val bitmap = it.image?.let { img -> ImageConverter.convertStringToBase64(img) }
                editTitle.setText(it.title)
                editMessage.setText(it.message)
                tvLongitude.text = it.longitude
                tvLatitude.text = it.latitude
                imagePreview.setImageBitmap(bitmap)
            }
        }
    }

    private fun saveNote() {
        val title = editTitle.text.toString()
        val message = editMessage.text.toString()
        val imageString = ImageConverter.convertDrawableToString(imagePreview.drawable)
        val longitude = tvLongitude.text.toString()
        val latitude = tvLatitude.text.toString()

        note?.apply {
            this.title = title
            this.message = message
            this.image = imageString
            this.longitude = longitude
            this.latitude = latitude
            noteDao?.update(this)
        } ?: noteDao?.insertAll(Note(title, message, imageString, longitude, latitude))

        finish()
    }

    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("longitude", tvLongitude.text)
        intent.putExtra("latitude", tvLatitude.text)
        intent.putExtra("title", editTitle.text.toString())
        intent.putExtra("message", editMessage.text.toString())
        startActivity(intent)
    }

    // Suppress Permission, because IDE does not recognize permission check in other method
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }).addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(
                        this,
                        resources.getString(R.string.permissions_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    tvLongitude = findViewById(R.id.editLongitude)
                    tvLatitude = findViewById(R.id.editLatitude)
                    tvLongitude.text = location?.longitude.toString()
                    tvLatitude.text = location?.latitude.toString()
                }

            }
        } else {
            requestPermissions()
        }
    }

    // check for permission
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permissions
    private fun requestPermissions() {
        permissionRequest.launch(locationPermissions)
    }

    // Permission result
    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_granted),
                    Toast.LENGTH_SHORT
                ).show()

                // Tries to get location again, after permissions are granted
                getLocation()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_denied),
                    Toast.LENGTH_SHORT
                ).show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
