package com.example.sololeveling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopVpnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("VPN", "BroadcastReceiver triggered") // Check if this appears in Logcat

        if (intent?.action == "STOP_VPN") {
            Log.d("VPN", "BroadcastReceiver received STOP_VPN intent")

            val stopIntent = Intent(context, MyVpnService::class.java)
            stopIntent.action = "FORCE_STOP_VPN" // Send this action to trigger stopVPN() inside the service
            context?.startService(stopIntent)
        }
    }
}

