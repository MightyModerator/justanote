// Declare the package name for the Kotlin file, which is part of the namespace for the app.
package com.example.myapplication

// Import various classes needed for the app's functionality.
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
// Import classes related to the app's database and utility functions.
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


// Define NoteEditActivity class which extends AppCompatActivity and implements interfaces for dialog and location updates.
class NoteEditActivity : AppCompatActivity(), DialogInterface.OnClickListener {

    // Note variables
    // Declare variables for data access object, note entity, UI components, and permission code.
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

    // Override the onCreate function to initialize the activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the layout defined in activity_note_edit.xml.
        setContentView(R.layout.activity_note_edit)
        // Set up the toolbar with navigation and custom icon.
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
        // Initialize UI components by finding them in the layout.
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
        // Setup the database and data access object.
        val db = Room.databaseBuilder(
            applicationContext, NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
    }

    private fun getExistingNote() {
        // Check if there is a note ID passed with the intent and load the note if it exists.
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
        // Gather all information from the input fields.
        val title = editTitle.text.toString()
        val message = editMessage.text.toString()
        val imageString = ImageConverter.convertDrawableToString(imagePreview.drawable)
        val longitude = tvLongitude.text.toString()
        val latitude = tvLatitude.text.toString()

        // Update an existing note or create a new one in the database.
        note?.apply {
            this.title = title
            this.message = message
            this.image = imageString
            this.longitude = longitude
            this.latitude = latitude
            noteDao?.update(this)
        } ?: noteDao?.insertAll(Note(title, message, imageString, longitude, latitude))

        // Close the activity after saving.
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

    // Suppresses the MissingPermission warning because the permission check is done in another method.
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            // If permissions are granted, get the current location with high accuracy.
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }).addOnSuccessListener { location: Location? ->
                    // If the location is null, show a toast message indicating permission denial.
                    if (location == null)
                    Toast.makeText(
                        this,
                        resources.getString(R.string.permissions_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                        // If the location is not null, update the UI with the new longitude and latitude.
                    tvLongitude = findViewById(R.id.editLongitude)
                    tvLatitude = findViewById(R.id.editLatitude)
                    tvLongitude.text = location?.longitude.toString()
                    tvLatitude.text = location?.latitude.toString()
                }

            }
        } else {
            // If permissions are not granted, request them.
            requestPermissions()
        }
    }

    // check for permission
    // Checks if both coarse and fine location permissions have been granted.
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permissions
    // Requests location permissions using the new permission request API.
    private fun requestPermissions() {
        permissionRequest.launch(locationPermissions)
    }

    // Permission result
    // Handles the result of the permission request.
    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all requested permissions have been granted.
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                // If permissions are granted, show a toast and attempt to get the location again.
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_granted),
                    Toast.LENGTH_SHORT
                ).show()

                // Tries to get location again, after permissions are granted
                getLocation()
            } else {
                // If permissions are denied, show a toast message.
                Toast.makeText(
                    this,
                    resources.getString(R.string.permissions_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Shows a dialog to confirm deletion of an item.
    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes), this)
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // Shares the current note if it has not been modified.
    private fun shareNote() {
        val currentTitle = editTitle.text.toString()
        val currentMessage = editMessage.text.toString()

        // Check if the note has been modified since it was last saved.
        val isModified = note?.title != currentTitle || note?.message != currentMessage

        if (isModified) {
            // If modified, show a toast message indicating that the note hasn't been saved yet.
            Toast.makeText(this, getString(R.string.share_not_saved), Toast.LENGTH_LONG).show()
        } else {
            // If not modified, prepare and send the share intent with the note's title and message.
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

    // Handles action bar item clicks.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            // If the location button is pressed, start the MapsActivity.
            R.id.location -> {
                startMapsActivity()
                true
            }

            // If the delete button is pressed, show the delete confirmation dialog.
            R.id.del -> {
                showDeleteDialog()
                true
            }

            // If the share button is pressed, execute the shareNote function.
            R.id.share -> {
                shareNote()
                true
            }

            // Default case: pass the event to the superclass.
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handles the results of permission requests.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Inflates the options menu.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handles clicks on the dialog interface.
    override fun onClick(p0: DialogInterface?, p1: Int) {
        note?.let {
            noteDao?.delete(it)

            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_LONG).show()

            // Finish the activity after deletion.
            finish()
        }
    }
}
