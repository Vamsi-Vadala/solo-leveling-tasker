package com.example.sololeveling

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.core.app.NotificationCompat

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("VPN", "onStartCommand received an intent")

        if (intent?.action == "FORCE_STOP_VPN") {
            Log.d("VPN", "Received FORCE_STOP_VPN intent")
            stopVPN() // 🔥 Now this will explicitly be called
            return START_NOT_STICKY
        }

        Log.d("VPN", "VPN Service Started")

        val builder = Builder()
        builder.setSession("BlockAppsVPN")
            .addAddress("10.0.0.2", 24)  // Fake local VPN IP
            .addRoute("0.0.0.0", 0)  // Route all traffic through VPN
            .addRoute("::", 0)  // Route IPv6 traffic (if supported)
            .addDnsServer("8.8.8.8") // Google DNS to prevent leaks
            .addDnsServer("1.1.1.1") // Cloudflare DNS
            .allowFamily(OsConstants.AF_INET) // Ensure IPv4 traffic is captured
            .allowFamily(OsConstants.AF_INET6) // Ensure IPv6 traffic is captured

        val sharedPreferences = getSharedPreferences("BlockedApps", MODE_PRIVATE)
        val blockedApps = sharedPreferences.all

//        builder.addAllowedApplication("com.example.sololeveling") // Only allow your app
//        builder.addAllowedApplication("com.instagram.android")
//        builder.addAllowedApplication("com.whatsapp")
//        builder.addAllowedApplication("com.facebook.katana")

//        Log.d("VPN", "Forcing Instagram, WhatsApp, and Facebook through VPN")
        for ((packageName, isBlocked) in blockedApps) {
            if (isBlocked as Boolean) {
                try {
                    builder.addAllowedApplication(packageName)
                    Log.d("VPN", "Blocking app: $packageName")
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e("VPN", "App not found: $packageName")
                }
            }
        }
//
//        for ((packageName, isBlocked) in blockedApps) {
//            if (isBlocked as Boolean) {
//                try {
//                    builder.addDisallowedApplication(packageName)
//                    Log.d("VPN", "Blocking app: $packageName")
//                } catch (e: PackageManager.NameNotFoundException) {
//                    Log.e("VPN", "App not found: $packageName")
//                }
//            }
//        }

        vpnInterface = builder.establish()

        // Run VPN in foreground to prevent Android from stopping it
        val channelId = "vpn_channel"
        val channel = NotificationChannel(
            channelId,
            "VPN Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VPN Active")
            .setContentText("Blocking selected apps")
            .setSmallIcon(R.drawable.ic_vpn)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
        Log.d("VPN", "VPN Service Stopped")
    }

    fun stopVPN() {
        stopForeground(true)
        stopSelf()
        onDestroy()
        Log.d("VPN", "Stopping VPN Service")
    }
}


