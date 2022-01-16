package com.example.locatefriends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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

class SignUp : AppCompatActivity() {

    lateinit var serverip: String
    lateinit var username: EditText
    lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        username = findViewById(R.id.userID_input)
        password = findViewById(R.id.password_input)
        serverip = resources.getString(R.string.ip)
    }

    fun trySignUp(view: View) {
        val queue = Volley.newRequestQueue(this)
        val url = serverip + "signup"

        val newJson = JSONObject()
        newJson.put("user",username.text.toString())
        newJson.put("password",password.text.toString())
        val requestStr = newJson.toString()

        val stringRequest = object: StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                val reJson = Klaxon().parseJsonObject(StringReader(response))
                //Toast.makeText(this,reJson["reply"].toString(),Toast.LENGTH_SHORT).show()

                if (reJson["reply"] == "pass") {
                    Toast.makeText(this, "Account successfully created", Toast.LENGTH_LONG).show()
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goToMainB(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}