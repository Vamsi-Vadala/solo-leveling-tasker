package com.example.sololeveling

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class PinEntryActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)

        setContent {
            PinScreen { pin ->
                if (pin == prefs.getString("pin", "")) {
                    finish()
                } else {
                    Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun PinScreen(onPinEntered: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Enter PIN", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Button(onClick = { onPinEntered(pin) }, modifier = Modifier.fillMaxWidth()) {
            Text("Unlock")
        }
    }
}
