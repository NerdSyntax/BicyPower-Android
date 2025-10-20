package com.example.bicypower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bicypower.navigation.AppNavGraph
import com.example.bicypower.ui.theme.BicyPowerTheme
import com.example.bicypower.data.local.database.BicyPowerDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // abre la DB (seed)
        BicyPowerDatabase.getInstance(applicationContext)

        setContent {
            BicyPowerTheme {
                AppNavGraph()
            }
        }
    }
}
