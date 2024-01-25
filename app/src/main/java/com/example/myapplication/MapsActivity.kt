// Define the package for this Kotlin file as 'com.example.myapplication'
package com.example.myapplication

// Import necessary Android and Google Maps classes.
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Declare MapsActivity class that inherits from AppCompatActivity and implements OnMapReadyCallback.
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Declare a GoogleMap object that will be initialized later.
    private lateinit var mMap: GoogleMap

    // Initialize default values for longitude, latitude, title, and message.
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var title: String = "Default Title"
    private var message: String = "Default Message"


    // Override the onCreate method which is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the user interface layout for this Activity from activity_maps.xml.
        setContentView(R.layout.activity_maps)

        // Set up the toolbar as the app bar for the activity.
        setSupportActionBar(findViewById(R.id.tbMap))
        // Enable the Up button for more navigation options.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set a custom icon for the Up button.
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Retrieve data passed to this activity through an intent and initialize variables.
        longitude = intent.getStringExtra("longitude")?.toDouble()!!
        latitude = intent.getStringExtra("latitude")?.toDouble()!!
        title = intent.getStringExtra("title") ?: "Default Title"
        message = intent.getStringExtra("message") ?: "Default Message"


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    // Handle options item selection in the toolbar menu.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Handle the action when the home/up button is pressed.
            android.R.id.home -> {
                finish()
                // Close this activity and return to the previous
                true
            }

            R.id.normal_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.hybrid_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            R.id.satellite_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we add markers and move the camera.
     * This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker to the location of the note and move the camera
        // Create a LatLng object with the latitude and longitude values.
        val noteLoc = LatLng(latitude, longitude)
        val cameraZoom = 10f
        // Add a marker on the map at the note location with a title and snippet (message).
        mMap.addMarker(
            MarkerOptions()
                .position(noteLoc)
                .title(title)
                .snippet(message)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_location))
        )
        // Move the camera to the note location with specified zoom level.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noteLoc, cameraZoom))
    }

}