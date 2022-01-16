package com.example.locatefriends

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class YourService : Service() {
    var alarm = Alarm()
    override fun onCreate() {
        super.onCreate()
        //Toast.makeText(this,"Alarm6 !!!!!!!!!!", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Toast.makeText(this,"Alarm3 !!!!!!!!!!", Toast.LENGTH_SHORT).show()
        alarm.setAlarm(this)
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
        //Toast.makeText(this,"Alarm2 !!!!!!!!!!", Toast.LENGTH_SHORT).show()
        alarm.setAlarm(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        alarm.cancelAlarm(this)
    }
}