package com.example.locationbasedservices

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*


class MainActivity : FragmentActivity(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private val permissionCode = 101
    private lateinit var alertDialog: AlertDialog
    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var longitudeTV: TextView
    private lateinit var latitudeTV: TextView
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        longitudeTV = findViewById(R.id.logitudeTV)
        latitudeTV = findViewById(R.id.latitudeTV)

        val supportMapFragment =
            (supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment?)!!
        supportMapFragment.getMapAsync(this@MainActivity)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

    }

    override fun onStart() {
        super.onStart()
        checkPermissionForGps()
        Log.e("TAG", "start")
    }
//
    private fun checkPermissionForGps() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
        } else {
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener {
                currentLocation = it
                updateLocation()
            }

            Toast.makeText(this, "Getting new location", Toast.LENGTH_SHORT).show()
            val mLocationRequest = LocationRequest.create()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            val callback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    currentLocation = p0.lastLocation
                    fusedLocationProviderClient.removeLocationUpdates(this)
                    updateLocation()
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                callback,
                Looper.getMainLooper()
            )
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        updateLocation()
    }

    private fun updateLocation() {
        currentLocation?.let {
            googleMap?.apply {
                clear()
                val latLng = LatLng(it.latitude, it.longitude)
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                val list: List<Address> =
                    geocoder.getFromLocation(currentLocation!!.latitude,currentLocation!!.longitude, 1)
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                mapType = GoogleMap.MAP_TYPE_HYBRID
                longitudeTV.text = "${list[0].locality}"
                latitudeTV.text = "${list[0].getAddressLine(0)}"
                Log.d("details", "longitude ${latLng.longitude}, Latitude ${latLng.latitude}")


                //Main Marker
                addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Here!!")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mukul1234))
                        .snippet("Well done you're on Right place")
                )

////marker 2
//                val latlng2 = LatLng(it.latitude, it.longitude + 0.005f)
//                addMarker(
//                    MarkerOptions()
//                        .position(latlng2)
//                        .title("marker2")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                )
////
                //Marker 3
//                val latlng3 = LatLng(it.latitude, it.longitude - 0.005f)
//                addMarker(
//                    MarkerOptions()
//                        .position(latlng3)
//                        .title("marker3")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
//                )

                 //Polygon shape

//                val polygonOptions = PolygonOptions()
//                    .add(LatLng(latLng.latitude, latLng.longitude))
//                    .add(LatLng(latLng.latitude + 0.010, latLng.longitude))
//                    .add(LatLng(latLng.latitude + 0.010, latLng.longitude + 0.006))
//                    .add(LatLng(latLng.latitude, latLng.longitude + 0.006))
//                addPolygon(polygonOptions)



                 //Circle Shape in Map

//                val circleOptions =
//                    CircleOptions()
//                        .center(LatLng(latLng.latitude, latLng.longitude))
//                        .strokeWidth(7f)
//                        .radius(500.0)
//                        .strokeColor(Color.GREEN)
//                        .fillColor(Color.argb(50, 255, 0, 0))
//                        .clickable(true)
//                addCircle(circleOptions)
//                setOnCircleClickListener {
//                    val strokeColor = it.strokeColor xor 0x00fff000
//                    it.strokeColor = strokeColor
//
//                }

            }

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                checkGpsState()
            }

        }
    }

    private fun checkGpsState() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!gpsEnabled && !networkEnabled) {
            alertDialog = AlertDialog.Builder(this)
                .setMessage("Location Not Allowed")
                .setPositiveButton("Enable Location") { dialog, which ->
                    dialog.dismiss()
                    this.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            checkPermissionForGps()
        }
    }
}