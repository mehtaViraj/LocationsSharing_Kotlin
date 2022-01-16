package com.example.locatefriends

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.StringReader

class Dashboard : AppCompatActivity() {

    lateinit var serverip: String
    lateinit var username: String
    lateinit var commPass: String
    lateinit var friendsStr: String
    lateinit var spinner: Spinner
    private var locationPermissionGranted = false
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    lateinit var lat: String
    lateinit var lng: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashbaord)

        serverip = resources.getString(R.string.ip)
        spinner = findViewById(R.id.friendsSpinner)

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        commPass = sharedPref.getString("commPass", "Null").toString()
        username = sharedPref.getString("username", "User ID").toString()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationPermission()

        getDeviceLocation(this)

        val intent = Intent(this, YourService::class.java)
        startService(intent)

        getFriends()
    }

    fun getDeviceLocation(context: Context) {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            //Toast.makeText(this, lastKnownLocation.toString(), Toast.LENGTH_LONG).show()
                            lat = lastKnownLocation?.latitude.toString()
                            lng = lastKnownLocation?.longitude.toString()

                            sendPost(this)
                        }
                    } else {
                        Log.d("LG", "Current location is null. Using defaults.")
                        Log.e("LG", "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun sendPost(context: Context) {

        val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        val commPass = sharedPref.getString("commPass", "Null").toString()
        val username = sharedPref.getString("username", "User ID").toString()

        val serverip = context.getString(R.string.ip)

        //Toast.makeText(context, "function called", Toast.LENGTH_LONG).show()

        val queue = Volley.newRequestQueue(context)
        val url = serverip + "uploadlocation"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        newJson.put("lat", lat)
        newJson.put("lng", lng)

        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    //
                }
                else {
                    Toast.makeText(context, reJson["error"].toString(), Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { Toast.makeText(context,"OOPS", Toast.LENGTH_SHORT).show() })
        {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return requestStr.toByteArray()
            }
        }
        queue.add(stringRequest)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    fun getFriends() {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"getfriends"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    friendsStr = reJson["friends"].toString()
                    populateSpinner()
                }
                else {
                    Toast.makeText(this, reJson["error"].toString(), Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { Toast.makeText(this,"OOPS", Toast.LENGTH_SHORT).show() })
        {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return requestStr.toByteArray()
            }
        }
        queue.add(stringRequest)
    }

    fun populateSpinner() {
        fun stringToWords(s : String) = s.trim().splitToSequence(',')
            .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
            .toList()
        val friendsList: List<String> = stringToWords(friendsStr)

        ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, friendsList).also  { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }

    fun logOut(view: View) {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"logout"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    goToMain()
                }
                else {
                    Toast.makeText(this, reJson["error"].toString(), Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { Toast.makeText(this,"OOPS", Toast.LENGTH_SHORT).show() })
        {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return requestStr.toByteArray()
            }
        }
        queue.add(stringRequest)
    }

    fun goToMain() {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("username", "User ID")
            putString("password", "Password")
            putString("commPass", "Null")
            putString("isLoggedIn", "0")
            commit()
        }

        val serviceIntent = Intent(this, YourService::class.java)
        stopService(serviceIntent)

        Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun removeFriend(view: View) {
        val selected: String = spinner.selectedItem.toString()
        //Toast.makeText(this, selected, Toast.LENGTH_SHORT).show()

        val queue = Volley.newRequestQueue(this)
        val url = serverip+"removefriend"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        newJson.put("target", selected)
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    getFriends()
                }
                else {
                    Toast.makeText(this, reJson["error"].toString(), Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { Toast.makeText(this,"OOPS", Toast.LENGTH_SHORT).show() })
        {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return requestStr.toByteArray()
            }
        }
        queue.add(stringRequest)
    }
    
    fun goToSocialPage(view: View) {
        val intent = Intent(this, SocialPage::class.java)
        startActivity(intent)
    }

    fun goToFriendMap(view: View) {
        val selected: String = spinner.selectedItem.toString()

        val intent = Intent(this, FriendPin::class.java).apply {
            putExtra("target", selected)
        }
        startActivity(intent)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}