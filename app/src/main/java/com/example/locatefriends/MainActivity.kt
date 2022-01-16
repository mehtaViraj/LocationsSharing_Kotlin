package com.example.locatefriends

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import org.json.JSONObject
import java.io.StringReader

class MainActivity : AppCompatActivity() {

    lateinit var serverip: String
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var commPassStr: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = findViewById(R.id.userID_input)
        password = findViewById(R.id.password_input)
        serverip = resources.getString(R.string.ip)

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        val userSaved: String? = sharedPref.getString("username", "User ID")
        val passSaved: String? = sharedPref.getString("password", "Password")
        val isLoggedIn: String? = sharedPref.getString("isLoggedIn", "0")

        username.setText(userSaved)
        password.setText(passSaved)

        if (isLoggedIn == "1") {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
    }

    fun tryLogin(view: View) {
        val queue = Volley.newRequestQueue(this)
        val url = serverip+"login"

        val newJson = JSONObject()
        newJson.put("user",username.text.toString())
        newJson.put("password",password.text.toString())
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    val reJson = Klaxon().parseJsonObject(StringReader(response))
                    //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                    if (reJson["reply"] == "pass") {
                        commPassStr = reJson["commPass"].toString()
                        goToDash()
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

    fun goToDash() {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("username", username.text.toString())
            putString("password", password.text.toString())
            putString("commPass", commPassStr)
            putString("isLoggedIn", "1")
            commit()
        }
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
    }

    fun goToSignUp(view:View) {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }

}