package com.example.locatefriends

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
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

class Alarm : BroadcastReceiver() {

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onReceive(context: Context, intent: Intent) {
        //Toast.makeText(context,"Alarm4 !!!!!!!!!!",Toast.LENGTH_SHORT).show()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val pm =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @SuppressLint("InvalidWakeLockTag") val wl =
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "")
        wl.acquire(10*60*1000L /*10 minutes*/)

        // Put here YOUR code.
        getDeviceLocation(context)
        //Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_SHORT).show() // For example
        wl.release()
    }

    fun setAlarm(context: Context) {

        val am =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, Alarm::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        //Toast.makeText(context, "Alarm7 !!!!!!!!!!", Toast.LENGTH_SHORT).show()

        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            1000 * 60 * 1.toLong(),
            pi
        ) // Millisec * Second * Minute
    }

    fun cancelAlarm(context: Context) {
        val intent = Intent(context, Alarm::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    fun getDeviceLocation(context: Context) {
        try {
            val locationResult = fusedLocationClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        //Toast.makeText(context, lastKnownLocation.toString(), Toast.LENGTH_LONG).show()
                        val lat = lastKnownLocation?.latitude.toString()
                        val lng = lastKnownLocation?.longitude.toString()
                        sendPost(context, lat, lng)
                    }
                } else {
                    Log.d("LG", "Current location is null. Using defaults.")
                    Log.e("LG", "Exception: %s", task.exception)
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun sendPost(context: Context, lat:String, lng:String) {

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
    }

