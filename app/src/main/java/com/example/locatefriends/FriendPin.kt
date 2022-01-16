package com.example.locatefriends

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.StringReader

class FriendPin : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var serverip: String
    lateinit var username: String
    lateinit var commPass: String
    lateinit var target: String
    lateinit var friendLat: String
    lateinit var friendLong: String
    lateinit var friendLastSeen: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_pin)

        serverip = resources.getString(R.string.ip)

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        commPass = sharedPref.getString("commPass", "Null").toString()
        username = sharedPref.getString("username", "User ID").toString()

        target = intent.getStringExtra("target").toString()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getFriendLocation()
    }

    fun getLocButtonWrapper(view: View) {
        getFriendLocation()
    }

    fun getFriendLocation() {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"getfriendlocation"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        newJson.put("target", target)
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    friendLat = reJson["lat"].toString()
                    friendLong = reJson["lng"].toString()
                    friendLastSeen = reJson["lastSeen"].toString()

                    resetMarker()
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

    fun resetMarker() {
        mMap.clear()

        val coord = LatLng(friendLat.toDouble() , friendLong.toDouble())
        mMap.addMarker(MarkerOptions().position(coord).title("$target at $friendLastSeen"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coord))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0F))
    }
}