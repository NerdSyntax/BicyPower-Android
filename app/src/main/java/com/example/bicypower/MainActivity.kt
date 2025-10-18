package com.example.bicypower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bicypower.navigation.AppNavGraph
import com.example.bicypower.ui.theme.BicyPowerTheme // ajusta el import si tu theme se llama distinto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BicyPowerTheme {
                AppNavGraph()   // <- SIN parámetros
            }
        }
    }
}
