package com.stepandemianenko.dsaprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stepandemianenko.dsaprep.navigation.AppNavigation
import com.stepandemianenko.dsaprep.ui.theme.DsaPrepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DsaPrepTheme {
                AppNavigation()
            }
        }
    }
}
