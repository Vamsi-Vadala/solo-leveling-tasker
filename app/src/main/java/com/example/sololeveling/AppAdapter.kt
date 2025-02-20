package com.example.sololeveling

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val appList: List<ApplicationInfo>, private val sharedPreferences: SharedPreferences) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appList[position]
        val pm = holder.itemView.context.packageManager
        holder.appName.text = pm.getApplicationLabel(appInfo)
        holder.appIcon.setImageDrawable(pm.getApplicationIcon(appInfo))

        val isBlocked = sharedPreferences.getBoolean(appInfo.packageName, false)
        holder.blockSwitch.isChecked = isBlocked

        holder.blockSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(appInfo.packageName, isChecked).apply()
        }
    }

    override fun getItemCount(): Int = appList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
        val blockSwitch: Switch = itemView.findViewById(R.id.blockSwitch)
    }
}
