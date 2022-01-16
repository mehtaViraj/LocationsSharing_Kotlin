package com.example.locatefriends

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import org.json.JSONObject
import java.io.StringReader

class SocialPage : AppCompatActivity() {

    lateinit var socialCodeDisplay: TextView
    lateinit var addFriendBox: EditText
    lateinit var serverip: String
    lateinit var username: String
    lateinit var commPass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_page)

        socialCodeDisplay = findViewById(R.id.socialCodeDisplay)
        addFriendBox = findViewById(R.id.addFriendsField)
        serverip = resources.getString(R.string.ip)

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        commPass = sharedPref.getString("commPass", "Null").toString()
        username = sharedPref.getString("username", "User ID").toString()

        updateSocialCodeDisplay()
    }

    fun changeSocialCode(view: View) {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"changesocialcode"

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
                    updateSocialCodeDisplay()
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

    fun updateSocialCodeDisplay() {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"getsocialcode"

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
                    socialCodeDisplay.setText(reJson["socialCode"].toString())
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

    fun  addFriend(view: View) {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"addfriend"

        val newJson = JSONObject()
        newJson.put("user", username)
        newJson.put("commPass", commPass)
        newJson.put("target", addFriendBox.text)
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    Toast.makeText(this, "Successfully added friend!", Toast.LENGTH_SHORT).show()
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

    fun goToDash(view: View) {
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
    }
}